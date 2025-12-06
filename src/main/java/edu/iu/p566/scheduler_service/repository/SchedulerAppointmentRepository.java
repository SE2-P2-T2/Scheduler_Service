package edu.iu.p566.scheduler_service.repository;

import edu.iu.p566.scheduler_service.model.SchedulerAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulerAppointmentRepository extends JpaRepository<SchedulerAppointment, Long> {

    List<SchedulerAppointment> findByStudentId(Long studentId);

    List<SchedulerAppointment> findByAppointmentId(Long appointmentId);

    List<SchedulerAppointment> findByGroupId(Long groupId);

    List<SchedulerAppointment> findByStudentIdAndStatus(Long studentId, String status);

    List<SchedulerAppointment> findByStatus(String status);

    List<SchedulerAppointment> findByBookingType(String bookingType);

    List<SchedulerAppointment> findByBookingTypeAndStatus(String bookingType, String status);

    List<SchedulerAppointment> findByGroupIdAndStatus(Long groupId, String status);
}