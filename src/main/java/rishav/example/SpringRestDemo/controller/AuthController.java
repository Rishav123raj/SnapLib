package rishav.example.SpringRestDemo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import rishav.example.SpringRestDemo.model.Account;
import rishav.example.SpringRestDemo.payload.auth.AccountDTO;
import rishav.example.SpringRestDemo.payload.auth.AccountViewDTO;
import rishav.example.SpringRestDemo.payload.auth.AuthoritiesDTO;
import rishav.example.SpringRestDemo.payload.auth.PasswordDTO;
import rishav.example.SpringRestDemo.payload.auth.ProfileDTO;
import rishav.example.SpringRestDemo.payload.auth.TokenDTO;
import rishav.example.SpringRestDemo.payload.auth.UserLoginDTO;
import rishav.example.SpringRestDemo.service.AccountService;
import rishav.example.SpringRestDemo.service.TokenService;
import rishav.example.SpringRestDemo.util.constants.AccountError;
import rishav.example.SpringRestDemo.util.constants.AccountSuccess;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Controller", description = "Controller for account management")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountService accountService;

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TokenDTO> token(@RequestBody UserLoginDTO userLogin) throws AuthenticationException {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword()));
            return ResponseEntity.ok(new TokenDTO(tokenService.generateToken(authentication)));
        } catch (Exception e) {
            log.debug(AccountError.TOKEN_GENERATION_ERROR.toString() + ":" + e.getMessage());
            return new ResponseEntity<>(new TokenDTO(null), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/users/add", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please enter a valid email and password between length 6 and 20")
    @ApiResponse(responseCode = "200", description = "Congratulations ! Your account has been added")
    @Operation(summary = "Add a new User")
    public ResponseEntity<String> addUser(@Valid @RequestBody AccountDTO accountDTO) {
        try {
            // Check if the rawPassword is null before proceeding
            if (accountDTO.getPassword() == null) {
                log.debug(AccountError.ADD_ACCOUNT_ERROR.toString() + ": rawPassword cannot be null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AccountError.ADD_ACCOUNT_ERROR.toString());
            }

            log.debug("Received password: " + accountDTO.getPassword());

            // Create a new Account instance and set the properties
            Account account = new Account();
            account.setEmail(accountDTO.getEmail());
            account.setPassword(accountDTO.getPassword());
            accountService.save(account);
            return ResponseEntity.ok(AccountSuccess.ACCOUNT_ADDED.toString());
        } catch (Exception e) {
            log.debug(AccountError.ADD_ACCOUNT_ERROR.toString() + ":" + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AccountError.ADD_ACCOUNT_ERROR.toString());
        }
    }

    @GetMapping(value = "/users", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of Account Users")
    @ApiResponse(responseCode = "401", description = "Token Missing")
    @ApiResponse(responseCode = "403", description = "Token Error")
    @Operation(summary = "List User Api")
    @SecurityRequirement(name = "rishaveasy-demo-api")
    public List<AccountViewDTO> Users() {
        List<AccountViewDTO> accountViewDTOs = new ArrayList<>();
        for (Account account : accountService.findall()) {
            accountViewDTOs.add(new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities()));
        }
        return accountViewDTOs;
    }

    @PutMapping(value = "/users/{user_id}/update-authorities", produces = "application/json", consumes = "application/json")
    @ApiResponse(responseCode = "200", description = "Update Authorities")
    @ApiResponse(responseCode = "401", description = "Token Missing")
    @ApiResponse(responseCode = "403", description = "Token Error")
    @Operation(summary = "Update Authorities")
    @SecurityRequirement(name = "rishaveasy-demo-api")
    public ResponseEntity<AccountViewDTO> update_auth(@Valid @RequestBody AuthoritiesDTO authoritiesDTO,
            @PathVariable long user_id) {
        Optional<Account> optionalAccount = accountService.findByID(user_id);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            account.setAuthorities(authoritiesDTO.getAuthorities());
            accountService.save(account);
            AccountViewDTO accountViewDTO = new AccountViewDTO(account.getId(), account.getEmail(),
                    account.getAuthorities());
            return ResponseEntity.ok(accountViewDTO);

        }
        return new ResponseEntity<AccountViewDTO>(new AccountViewDTO(), HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/profile", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "View Profile")
    @ApiResponse(responseCode = "401", description = "Token Missing")
    @ApiResponse(responseCode = "403", description = "Token Error")
    @Operation(summary = "View Profile")
    @SecurityRequirement(name = "rishaveasy-demo-api")
    public ProfileDTO profile(Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        ProfileDTO profileDTO = new ProfileDTO(account.getId(), account.getEmail(), account.getAuthorities());
        return profileDTO;
    }

    @PutMapping(value = "/profile/update-password", produces = "application/json", consumes = "application/json")
    @ApiResponse(responseCode = "200", description = "Update Password")
    @ApiResponse(responseCode = "401", description = "Token Missing")
    @ApiResponse(responseCode = "403", description = "Token Error")
    @Operation(summary = "Update Password")
    @SecurityRequirement(name = "rishaveasy-demo-api")
    public AccountViewDTO update_password(@Valid @RequestBody PasswordDTO passwordDTO, Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        account.setPassword(passwordDTO.getPassword());
        accountService.save(account);
        AccountViewDTO accountViewDTO = new AccountViewDTO(account.getId(), account.getEmail(),
                account.getAuthorities());
        return accountViewDTO;
    }

    @DeleteMapping(value = "/profile/delete")
    @ApiResponse(responseCode = "200", description = "Delete Profile")
    @ApiResponse(responseCode = "401", description = "Token Missing")
    @ApiResponse(responseCode = "403", description = "Token Error")
    @Operation(summary = "Delete Profile")
    @SecurityRequirement(name = "rishaveasy-demo-api")
    public ResponseEntity<String> delete_profile(Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        if (optionalAccount.isPresent()) {
            accountService.deletedByID(optionalAccount.get().getId());
            return ResponseEntity.ok("User Deleted");
        }
        return new ResponseEntity<String>("Bad Request", HttpStatus.BAD_REQUEST);
    }
}
