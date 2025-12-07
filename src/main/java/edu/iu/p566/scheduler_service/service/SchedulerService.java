package edu.iu.p566.scheduler_service.service;

import edu.iu.p566.scheduler_service.client.GroupMemberServiceClient;
import edu.iu.p566.scheduler_service.client.GroupServiceClient;
import edu.iu.p566.scheduler_service.client.UserServiceClient;
import edu.iu.p566.scheduler_service.dto.GroupAppointmentDTO;
import edu.iu.p566.scheduler_service.dto.GroupMemberDTO;
import edu.iu.p566.scheduler_service.dto.UserDTO;
import edu.iu.p566.scheduler_service.model.SchedulerAppointment;
import edu.iu.p566.scheduler_service.repository.SchedulerAppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final SchedulerAppointmentRepository schedulerRepository;
    private final UserServiceClient userServiceClient;
    private final GroupServiceClient groupServiceClient;
    private final GroupMemberServiceClient groupMemberServiceClient;

    @Transactional
    public SchedulerAppointment bookIndividualAppointment(Long studentId, Long appointmentId, String status, String description) {
        log.info("Booking individual appointment for student: {} and appointment: {}", studentId, appointmentId);

        UserDTO student = userServiceClient.getUserById(studentId);

        Integer roleId = student.getRoleId();
        if (roleId == null || roleId != 3) {
            throw new RuntimeException("User is not a student or role is not set");
        }

        SchedulerAppointment booking = SchedulerAppointment.builder()
                .studentId(studentId)
                .appointmentId(appointmentId)
                .bookingType("individual")
                .status(status)
                .notes(description)
                .bookedAt(OffsetDateTime.now())
                .build();

        return schedulerRepository.save(booking);
    }

    @Transactional
    public SchedulerAppointment bookGroupAppointment(Long studentId, Long groupId, String status, String description) {
        log.info("Booking group appointment for student: {} and group: {}", studentId, groupId);

        UserDTO student = userServiceClient.getUserById(studentId);

        Integer roleId = student.getRoleId();
        if (roleId == null || roleId != 3) {
            throw new RuntimeException("User is not a student or role is not set");
        }

        GroupAppointmentDTO group = groupServiceClient.getGroupById(groupId);
        List<GroupMemberDTO> currentMembers = groupMemberServiceClient.getMembersByGroupId(groupId);

        log.info("Group {} has {}/{} members", groupId, currentMembers.size(), group.getMaxLimit());

        if (currentMembers.size() >= group.getMaxLimit()) {
            throw new RuntimeException("Group appointment is full. Maximum capacity: " + group.getMaxLimit());
        }

        boolean alreadyInGroup = currentMembers.stream()
                .anyMatch(member -> member.getUserId().equals(studentId.intValue()));

        if (alreadyInGroup) {
            throw new RuntimeException("You have already joined this group appointment");
        }

        SchedulerAppointment booking = SchedulerAppointment.builder()
                .studentId(studentId)
                .groupId(groupId)
                .bookingType("group")
                .status(status)
                .notes(description)
                .bookedAt(OffsetDateTime.now())
                .build();

        SchedulerAppointment savedBooking = schedulerRepository.save(booking);
        log.info("Booking saved with ID: {}", savedBooking.getBookingId());

        try {
            GroupMemberDTO newMember = GroupMemberDTO.builder()
                    .groupId(groupId.intValue())
                    .userId(studentId.intValue())
                    .memberFirstName(student.getFirstName())
                    .memberLastName(student.getLastName())
                    .build();

            GroupMemberDTO savedMember = groupMemberServiceClient.addGroupMember(newMember);
            log.info("Student {} added to group_members with member_id: {}", studentId, savedMember.getMemberId());
        } catch (Exception e) {
            log.error("Failed to add student to group_members table: {}", e.getMessage());
            schedulerRepository.delete(savedBooking);
            throw new RuntimeException("Failed to add student to group. Please try again.", e);
        }

        return savedBooking;
    }

    @Transactional
    public void cancelAppointment(Long bookingId, String reason) {
        log.info("Cancelling booking: {} with reason: {}", bookingId, reason);

        SchedulerAppointment booking = schedulerRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if ("group".equals(booking.getBookingType()) && booking.getGroupId() != null) {
            try {
                groupMemberServiceClient.removeMemberFromGroup(
                        booking.getGroupId().intValue(),
                        booking.getStudentId().intValue()
                );
                log.info("Removed student {} from group_members for group {}",
                        booking.getStudentId(), booking.getGroupId());
            } catch (Exception e) {
                log.warn("Failed to remove from group_members: {}", e.getMessage());
            }
        }

        booking.setStatus("cancelled");
        booking.setCancelledAt(OffsetDateTime.now());
        booking.setCancellationReason(reason);

        schedulerRepository.save(booking);
    }

    @Transactional
    public void cancelGroupAppointmentForAll(Long bookingId, String reason) {
        log.info("Cancelling group appointment for all members: {} with reason: {}", bookingId, reason);

        SchedulerAppointment booking = schedulerRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"group".equals(booking.getBookingType())) {
            throw new RuntimeException("This is not a group appointment");
        }

        Long groupId = booking.getGroupId();
        List<SchedulerAppointment> allGroupBookings = schedulerRepository.findByGroupIdAndStatus(groupId, "confirmed");

        for (SchedulerAppointment groupBooking : allGroupBookings) {
            groupBooking.setStatus("cancelled");
            groupBooking.setCancelledAt(OffsetDateTime.now());
            groupBooking.setCancellationReason(reason + " (Group cancelled by member)");
            schedulerRepository.save(groupBooking);
        }

        try {
            groupMemberServiceClient.deleteAllMembersByGroupId(groupId.intValue());
            log.info("Removed all members from group_members for group {}", groupId);
        } catch (Exception e) {
            log.error("Failed to remove all members from group_members: {}", e.getMessage());
        }

        log.info("Cancelled {} group bookings and removed all members for group {}",
                allGroupBookings.size(), groupId);
    }

    public List<GroupMemberDTO> getGroupMembers(Long groupId) {
        log.info("Getting members for group: {}", groupId);
        return groupMemberServiceClient.getMembersByGroupId(groupId);
    }

    public List<SchedulerAppointment> getAppointmentsForStudent(Long studentId) {
        log.info("Getting appointments for student: {}", studentId);
        return schedulerRepository.findByStudentId(studentId);
    }


    public List<UserDTO> getAllInstructors() {
        log.info("Getting all instructors from User Service");
        List<UserDTO> allUsers = userServiceClient.getAllUsers();

        return allUsers.stream()
                .filter(user -> user.getRoleId() != null && user.getRoleId() == 1)
                .collect(Collectors.toList());
    }

    public UserDTO getInstructorById(Long instructorId) {
        log.info("Getting instructor by id: {}", instructorId);
        UserDTO user = userServiceClient.getUserById(instructorId);

        Integer roleId = user.getRoleId();
        if (roleId == null || roleId != 1) {
            throw new RuntimeException("User is not an instructor");
        }
        return user;
    }

    public UserDTO getStudentById(Long studentId) {
        log.info("Getting student by id: {}", studentId);
        UserDTO user = userServiceClient.getUserById(studentId);

        Integer roleId = user.getRoleId();
        if (roleId == null || roleId != 3) {
            throw new RuntimeException("User is not a student");
        }
        return user;
    }


    public List<SchedulerAppointment> getIndividualAppointmentBookingsByInstructor(Long instructorId) {
        log.info("Getting individual appointment bookings for instructor: {}", instructorId);
        return schedulerRepository.findByBookingTypeAndStatus("individual", "confirmed");
    }

    public List<SchedulerAppointment> getGroupAppointmentBookingsByInstructor(Long instructorId) {
        log.info("Getting group appointment bookings for instructor: {}", instructorId);
        return schedulerRepository.findByBookingTypeAndStatus("group", "confirmed");
    }

    public List<SchedulerAppointment> getIndividualAppointmentBookings() {
        log.info("Getting all individual appointment bookings");
        return schedulerRepository.findByBookingTypeAndStatus("individual", "confirmed");
    }

    public List<SchedulerAppointment> getGroupAppointmentBookings() {
        log.info("Getting all group appointment bookings");
        return schedulerRepository.findByBookingTypeAndStatus("group", "confirmed");
    }

    public List<SchedulerAppointment> getAllBookings() {
        log.info("Getting all bookings");
        return schedulerRepository.findAll();
    }

    public List<SchedulerAppointment> getBookingsByType(String bookingType) {
        log.info("Getting bookings by type: {}", bookingType);
        return schedulerRepository.findByBookingType(bookingType);
    }

    public List<SchedulerAppointment> getBookingsByStatus(String status) {
        log.info("Getting bookings by status: {}", status);
        return schedulerRepository.findByStatus(status);
    }
}