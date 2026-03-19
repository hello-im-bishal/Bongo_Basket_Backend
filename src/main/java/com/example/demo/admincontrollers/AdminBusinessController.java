package com.example.demo.admincontrollers;

import com.example.demo.adminservices.AdminBusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/admin/business")
@CrossOrigin(origins = "*")
public class AdminBusinessController {

    private final AdminBusinessService adminBusinessService;

    public AdminBusinessController(AdminBusinessService adminBusinessService) {
        this.adminBusinessService = adminBusinessService;
    }

    // =========================
    // MONTHLY BUSINESS
    // =========================
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyBusiness(@RequestParam Integer month,
                                                @RequestParam Integer year) {
        try {
            Map<String, Object> report =
                    adminBusinessService.calculateMonthlyBusiness(month, year);
            return ResponseEntity.ok(report);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while calculating monthly business");
        }
    }

    // =========================
    // DAILY BUSINESS
    // =========================
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyBusiness(@RequestParam String date) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            Map<String, Object> report =
                    adminBusinessService.calculateDailyBusiness(parsedDate);
            return ResponseEntity.ok(report);

        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid date format. Use yyyy-MM-dd");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while calculating daily business");
        }
    }

    // =========================
    // YEARLY BUSINESS
    // =========================
    @GetMapping("/yearly")
    public ResponseEntity<?> getYearlyBusiness(@RequestParam Integer year) {
        try {
            Map<String, Object> report =
                    adminBusinessService.calculateYearlyBusiness(year);
            return ResponseEntity.ok(report);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while calculating yearly business");
        }
    }

    // =========================
    // OVERALL BUSINESS
    // =========================
    @GetMapping("/overall")
    public ResponseEntity<?> getOverallBusiness() {
        try {
            Map<String, Object> report =
                    adminBusinessService.calculateOverallBusiness();
            return ResponseEntity.ok(report);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while calculating overall business");
        }
    }
}