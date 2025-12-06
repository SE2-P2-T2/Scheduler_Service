package edu.iu.p566.scheduler_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String username;

    private RoleDTO role;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDTO {
        private Integer roleId;
        private String roleName;
    }

    public Integer getRoleId() {
        return role != null ? role.getRoleId() : null;
    }
}