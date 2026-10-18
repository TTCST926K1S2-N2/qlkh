package vn.edu.ictu.qlkh.service;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class LeadSlaScheduler implements ServletContextListener {

    private static final Logger LOGGER =
            Logger.getLogger(LeadSlaScheduler.class.getName());

    private ScheduledExecutorService scheduler;
    private String contextPath = "";

    @Override
    public void contextInitialized(ServletContextEvent event) {

        contextPath = event.getServletContext().getContextPath();

        String enabledConfig = event.getServletContext()
                .getInitParameter("leadSlaSchedulerEnabled");

        boolean enabled = !"false".equalsIgnoreCase(
                enabledConfig == null ? null : enabledConfig.trim()
        );

        if (!enabled) {
            LOGGER.info(
                    "Lead SLA Scheduler disabled. " +
                    "Set leadSlaSchedulerEnabled=true to enable."
            );
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(
                runnable -> {
                    Thread thread = new Thread(
                            runnable,
                            "lead-sla-scheduler"
                    );
                    thread.setDaemon(true);
                    return thread;
                }
        );

        scheduler.scheduleWithFixedDelay(
                this::checkOverdueLeads,
                1,
                5,
                TimeUnit.MINUTES
        );

        LOGGER.info("Lead SLA Scheduler started");
    }

    private void checkOverdueLeads() {

        try {

            LeadSlaOverdueService service =
                    new LeadSlaOverdueService(contextPath);

            int notified = service.processOverdueLeads();

            if (notified > 0) {
                LOGGER.info(
                        "Lead SLA overdue notifications sent: "
                                + notified
                );
            }

        } catch (SQLException | RuntimeException ex) {

            LOGGER.log(
                    Level.SEVERE,
                    "Lead SLA Scheduler failed",
                    ex
            );
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

        if (scheduler != null) {

            scheduler.shutdown();

            try {

                if (!scheduler.awaitTermination(
                        10,
                        TimeUnit.SECONDS
                )) {
                    scheduler.shutdownNow();
                }

            } catch (InterruptedException ex) {

                scheduler.shutdownNow();

                Thread.currentThread().interrupt();
            }

            LOGGER.info("Lead SLA Scheduler stopped");
        }
    }
}