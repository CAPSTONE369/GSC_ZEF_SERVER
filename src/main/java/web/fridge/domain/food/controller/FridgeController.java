package web.fridge.domain.food.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.fridge.domain.food.controller.dto.FridgeMemberInviteDTO;
import web.fridge.domain.food.controller.dto.FridgeMemberWithdrawDTO;
import web.fridge.domain.food.controller.dto.FridgeResponseDTO;
import web.fridge.domain.food.entity.Fridge;
import web.fridge.domain.food.service.FridgeService;
import web.fridge.domain.invitation.dto.InvitationResponseDTO;
import web.fridge.domain.invitation.entity.Invitation;
import web.fridge.domain.invitation.service.InvitationService;
import web.fridge.domain.member.annotation.AuthMember;
import web.fridge.domain.member.controller.dto.MemberResponseDTO;
import web.fridge.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/fridges", produces = "application/json; charset=utf8")
public class FridgeController {
    private final FridgeService fridgeService;
    private final InvitationService invitationService;

    @GetMapping
    public ResponseEntity<?> fridgeListFind(@AuthMember Member member){
        List<Fridge> fridgeList = fridgeService.findFridgeListByMember(member);
        List<FridgeResponseDTO> responseDTOList = new ArrayList<>();
        for (Fridge fridge : fridgeList){
            responseDTOList.add(FridgeResponseDTO.builder().member(member).fridge(fridge).build());
        }
        return new ResponseEntity<>(responseDTOList, HttpStatus.OK);
    }

    @GetMapping("/{fridgeId}")
    public ResponseEntity<?> fridgeMemberListFind(@AuthMember Member member, @PathVariable Long fridgeId){
        List<Member> memberList = fridgeService.findMembersByFridge(fridgeId);
        List<MemberResponseDTO> responseDTOList = new ArrayList<>();
        for (Member fridgeMember : memberList) {
            responseDTOList.add(MemberResponseDTO.builder().member(member).build());
        }
        return new ResponseEntity<>(responseDTOList, HttpStatus.OK);
    }

    @PostMapping("/invite")
    public ResponseEntity<?> fridgeMemberInvite(@AuthMember Member member, @RequestBody FridgeMemberInviteDTO requestDTO){
        Invitation invitation = invitationService.inviteFridgeMember(member, requestDTO);
        InvitationResponseDTO invitationResponseDTO = new InvitationResponseDTO(invitation);
        return new ResponseEntity<>(invitationResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<?> fridgeMemberRemoveSelf(@AuthMember Member member, @RequestBody FridgeMemberWithdrawDTO requestDTO){
        fridgeService.removeFridgeMember(member, requestDTO);
        return new ResponseEntity<>("냉장고에서 탈퇴되셨습니다.", HttpStatus.OK);
    }

}
