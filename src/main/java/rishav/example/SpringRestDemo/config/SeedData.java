package rishav.example.SpringRestDemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import rishav.example.SpringRestDemo.model.Account;
import rishav.example.SpringRestDemo.service.AccountService;
import rishav.example.SpringRestDemo.util.constants.Authority;

@Component
public class SeedData implements CommandLineRunner {

    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
        Account account01 = new Account();
        Account account02 = new Account();

        account01.setEmail("users@gmail.com");
        account01.setPassword("user987");
        account01.setAuthorities(Authority.USER.toString());
        accountService.save(account01);

        account02.setEmail("admin123456@gmail.com");
        account02.setPassword("admin988");
        account02.setAuthorities(Authority.ADMIN.toString() + " " + Authority.USER.toString());
        accountService.save(account02);
    }

}
