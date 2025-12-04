package edu.iu.p566.scheduler_service.repository;

import edu.iu.p566.scheduler_service.model.SchedulerAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SchedulerAppointmentRepository extends JpaRepository<SchedulerAppointment, Long> {
    List<SchedulerAppointment> findByStudentId(Long studentId);
}
