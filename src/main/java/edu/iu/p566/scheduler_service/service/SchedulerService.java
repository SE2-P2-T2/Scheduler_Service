package edu.iu.p566.scheduler_service.service;

import edu.iu.p566.scheduler_service.client.GroupMemberServiceClient;
import edu.iu.p566.scheduler_service.client.GroupServiceClient;
import edu.iu.p566.scheduler_service.client.UserServiceClient;
import edu.iu.p566.scheduler_service.dto.GroupAppointmentDTO;
import edu.iu.p566.scheduler_service.dto.GroupMemberDTO;
import edu.iu.p566.scheduler_service.dto.UserDTO;
import edu.iu.p566.scheduler_service.messaging.BookingEventPublisher;
import edu.iu.p566.scheduler_service.model.BookingCreatedEvent;
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
    private final BookingEventPublisher bookingEventPublisher;

    private static final int MINIMUM_MEMBERS_TO_BOOK = 2;

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

        SchedulerAppointment saved = schedulerRepository.save(booking);

        // publish event
        BookingCreatedEvent.Payload payload = new BookingCreatedEvent.Payload();
        payload.bookingId = saved.getBookingId();
        payload.studentId = saved.getStudentId();
        payload.appointmentId = saved.getAppointmentId();
        payload.groupAppointmentId = saved.getGroupAppointmentId();
        payload.groupId = saved.getGroupId();
        payload.bookingType = saved.getBookingType();
        payload.status = saved.getStatus();
        payload.notes = saved.getNotes();

        bookingEventPublisher.publishBookingCreated(payload);

        return saved;
    }

    @Transactional
    public GroupMemberDTO joinGroup(Long studentId, Long groupId) {
        log.info("Student {} joining group {}", studentId, groupId);

        UserDTO student = userServiceClient.getUserById(studentId);

        Integer roleId = student.getRoleId();
        if (roleId == null || roleId != 3) {
            throw new RuntimeException("User is not a student or role is not set");
        }

        GroupAppointmentDTO group = groupServiceClient.getGroupById(groupId);
        if (Boolean.TRUE.equals(group.getIsBooked())) {
            throw new RuntimeException("This group has already been booked and is no longer accepting new members");
        }

        List<GroupMemberDTO> currentMembers = groupMemberServiceClient.getMembersByGroupId(groupId);

        Integer maxLimit = group.getMaxLimit() != null ? group.getMaxLimit() : 10;

        log.info("Group {} has {}/{} members", groupId, currentMembers.size(), maxLimit);

        if (currentMembers.size() >= maxLimit) {
            throw new RuntimeException("Group is full. Maximum capacity: " + maxLimit);
        }
        boolean alreadyInGroup = currentMembers.stream()
                .anyMatch(member -> member.getUserId().equals(studentId.intValue()));

        if (alreadyInGroup) {
            throw new RuntimeException("You have already joined this group");
        }
        GroupMemberDTO newMember = GroupMemberDTO.builder()
                .groupId(groupId.intValue())
                .userId(studentId.intValue())
                .build();

        GroupMemberDTO savedMember = groupMemberServiceClient.addGroupMember(newMember);

        return savedMember;
    }

    @Transactional
    public SchedulerAppointment bookGroupAppointmentForAll(Long studentId, Long groupId, String description, Long groupAppointmentId) {
        log.info("Student {} booking appointment for entire group {} with groupAppointmentId: {}", studentId, groupId, groupAppointmentId);

        UserDTO student = userServiceClient.getUserById(studentId);

        Integer roleId = student.getRoleId();
        if (roleId == null || roleId != 3) {
            throw new RuntimeException("User is not a student or role is not set");
        }

        GroupAppointmentDTO group = groupServiceClient.getGroupById(groupId);

        if (Boolean.TRUE.equals(group.getIsBooked())) {
            throw new RuntimeException("This group has already been booked");
        }

        List<GroupMemberDTO> members = groupMemberServiceClient.getMembersByGroupId(groupId);

        boolean isMember = members.stream()
                .anyMatch(member -> member.getUserId().equals(studentId.intValue()));

        if (!isMember) {
            throw new RuntimeException("You must be a member of this group to book it");
        }

        if (members.size() < MINIMUM_MEMBERS_TO_BOOK) {
            throw new RuntimeException("Group needs at least " + MINIMUM_MEMBERS_TO_BOOK +
                    " members to book. Current members: " + members.size());
        }

        SchedulerAppointment booking = SchedulerAppointment.builder()
                .studentId(studentId)
                .groupId(groupId)
                .groupAppointmentId(groupAppointmentId)
                .bookingType("group")
                .status("confirmed")
                .notes(description + " | Booked by: " + student.getFirstName() + " " + student.getLastName())
                .bookedAt(OffsetDateTime.now())
                .build();

        SchedulerAppointment savedBooking = schedulerRepository.save(booking);
        log.info("Booking created with ID: {} for group {} and groupAppointmentId: {}", savedBooking.getBookingId(), groupId, groupAppointmentId);

        // publish event
        BookingCreatedEvent.Payload payload = new BookingCreatedEvent.Payload();
        payload.bookingId = savedBooking.getBookingId();
        payload.studentId = savedBooking.getStudentId();
        payload.appointmentId = savedBooking.getAppointmentId();
        payload.groupAppointmentId = savedBooking.getGroupAppointmentId();
        payload.groupId = savedBooking.getGroupId();
        payload.bookingType = savedBooking.getBookingType();
        payload.status = savedBooking.getStatus();
        payload.notes = savedBooking.getNotes();

        bookingEventPublisher.publishBookingCreated(payload);

        return savedBooking;
    }

    @Transactional
    public void leaveGroup(Long studentId, Long groupId) {
        log.info("Student {} leaving group {}", studentId, groupId);

        GroupAppointmentDTO group = groupServiceClient.getGroupById(groupId);

        if (Boolean.TRUE.equals(group.getIsBooked())) {
            throw new RuntimeException("Cannot leave a group that has already been booked");
        }

        try {
            groupMemberServiceClient.removeMemberFromGroup(groupId.intValue(), studentId.intValue());
            log.info("Student {} removed from group {}", studentId, groupId);
        } catch (Exception e) {
            log.error("Failed to remove student from group: {}", e.getMessage());
            throw new RuntimeException("Failed to leave group", e);
        }
    }

    @Transactional
    public void cancelAppointment(Long bookingId, String reason) {
        log.info("Cancelling booking: {} with reason: {}", bookingId, reason);

        SchedulerAppointment booking = schedulerRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus("cancelled");
        booking.setCancelledAt(OffsetDateTime.now());
        booking.setCancellationReason(reason);

        schedulerRepository.save(booking);

        log.info("Booking cancelled. Group availability is determined by booking status.");
    }

    public List<GroupMemberDTO> getGroupMembers(Long groupId) {
        log.info("Getting members for group: {}", groupId);
        List<GroupMemberDTO> members = groupMemberServiceClient.getMembersByGroupId(groupId);

        members.forEach(member -> {
            try {
                UserDTO user = userServiceClient.getUserById(member.getUserId().longValue());
                member.setMemberFirstName(user.getFirstName());
                member.setMemberLastName(user.getLastName());
            } catch (Exception e) {
                log.error("Failed to fetch user details for userId: {}", member.getUserId(), e);
                member.setMemberFirstName("Unknown");
                member.setMemberLastName("User");
            }
        });

        return members;
    }

    public boolean isUserMemberOfGroup(Long studentId, Long groupId) {
        List<GroupMemberDTO> members = groupMemberServiceClient.getMembersByGroupId(groupId);
        return members.stream()
                .anyMatch(member -> member.getUserId().equals(studentId.intValue()));
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