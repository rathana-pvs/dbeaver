package org.jkiss.dbeaver.ext.cubrid.model;

import org.jkiss.dbeaver.model.exec.DBCStatistics;

public class CubridStatistics extends DBCStatistics{
	private String traceStatistic;

	
	public String getTraceStatistic() {
		return traceStatistic;
	}

	public void setTraceStatistic(String traceStatistic) {
		this.traceStatistic = traceStatistic;
	}
}
