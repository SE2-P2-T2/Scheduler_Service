package edu.iu.p566.scheduler_service.client;

import edu.iu.p566.scheduler_service.dto.GroupMemberDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "group-member-service", url = "${group.service.url:http://localhost:8080}")
public interface GroupMemberServiceClient {

    @GetMapping("/api/groupmembers/group/{groupId}")
    List<GroupMemberDTO> getMembersByGroupId(@PathVariable("groupId") Long groupId);

    @PostMapping("/api/groupmembers")
    GroupMemberDTO addGroupMember(@RequestBody GroupMemberDTO groupMember);

    @DeleteMapping("/api/groupmembers/group/{groupId}")
    void deleteAllMembersByGroupId(@PathVariable("groupId") Integer groupId);

    @DeleteMapping("/api/groupmembers/group/{groupId}/user/{userId}")
    void removeMemberFromGroup(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId
    );
}