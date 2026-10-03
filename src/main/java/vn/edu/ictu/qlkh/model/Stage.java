package vn.edu.ictu.qlkh.model;

import java.math.BigDecimal;

public class Stage {

    private long id;
    private String name;
    private String code;
    private int stageOrder;
    private BigDecimal winProbability;
    private String exitCondition;
    private String status;
    private String description;

    public Stage() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(int stageOrder) {
        this.stageOrder = stageOrder;
    }

    public BigDecimal getWinProbability() {
        return winProbability;
    }

    public void setWinProbability(BigDecimal winProbability) {
        this.winProbability = winProbability;
    }

    public String getExitCondition() {
        return exitCondition;
    }

    public void setExitCondition(String exitCondition) {
        this.exitCondition = exitCondition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}