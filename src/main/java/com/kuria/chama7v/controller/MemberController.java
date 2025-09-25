package com.kuria.chama7v.controller;

import com.kuria.chama7v.dto.request.MemberRegistrationRequest;
import com.kuria.chama7v.dto.response.ApiResponse;
import com.kuria.chama7v.dto.response.MemberResponse;
import com.kuria.chama7v.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<MemberResponse>> registerMember(
            @Valid @RequestBody MemberRegistrationRequest request) {
        try {
            MemberResponse memberResponse = memberService.registerMember(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Member registered successfully", memberResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateJoined") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MemberResponse> members = memberService.getAllMembers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Members retrieved successfully", members));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(@PathVariable Long id) {
        try {
            MemberResponse member = memberService.getMemberById(id);
            return ResponseEntity.ok(ApiResponse.success("Member retrieved successfully", member));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMemberProfile() {
        try {
            var currentMember = memberService.getCurrentMember();
            MemberResponse member = memberService.getMemberById(currentMember.getId());
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", member));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unable to retrieve profile"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberRegistrationRequest request) {
        try {
            MemberResponse updatedMember = memberService.updateMember(id, request);
            return ResponseEntity.ok(ApiResponse.success("Member updated successfully", updatedMember));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<Void>> suspendMember(@PathVariable Long id) {
        try {
            memberService.suspendMember(id);
            return ResponseEntity.ok(ApiResponse.success("Member suspended successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to suspend member"));
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<Void>> activateMember(@PathVariable Long id) {
        try {
            memberService.activateMember(id);
            return ResponseEntity.ok(ApiResponse.success("Member activated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to activate member"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CHAIRPERSON')")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        try {
            memberService.deleteMember(id);
            return ResponseEntity.ok(ApiResponse.success("Member deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete member"));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> searchMembers(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MemberResponse> members = memberService.searchMembers(searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search completed", members));
    }
}