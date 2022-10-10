package com.songyuankun.jd.job;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JdJobController {

    private final JdJob jdJob;

    public JdJobController(JdJob jdJob) {
        this.jdJob = jdJob;
    }

    @GetMapping("public/getJD")
    public String getJD(@RequestParam("type") Integer type) throws Exception {
        switch (type) {
            case 1:
                return jdJob.getJdSign();
            case 2:
                return jdJob.getSharkBean();
            case 3:
                return jdJob.getLottery();
            case 4:
                return jdJob.plusSign();
            default:
                return "fail";
        }
    }
}
