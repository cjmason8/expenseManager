package au.com.mason.expensemanager.processor;

import au.com.mason.expensemanager.config.SpringContext;

public enum EmailProcessor {
	DINGLEY_ELECTRICITY(DingleyElectricityProccesor.class),
	DINGLEY_GAS(DingleyGasProccesor.class),
	DINGLEY_WATER(DingleyWaterProccesor.class),
	WODONGA_WATER(WodongaWaterProccesor.class),
	WODONGA_RATES(WodongaRatesProccesor.class),
	WODONGA_INSURANCE(WodongaInsuranceProccesor.class),
	DINGLEY_INSURANCE(DingleyInsuranceProccesor.class),
	SOUTH_KINGSVILLE_RATES(SouthKingsvilleRatesProccesor.class),
	SOUTH_KINGSVILLE_WATER(SouthKingsvilleWaterProccesor.class),
	SOUTH_KINGSVILLE_INSURANCE(SouthKingsvilleInsuranceProccesor.class),
	RACV_MEMBERSHIP(RACVMembershipProccesor.class),
	CAMRY_INSURANCE(CamryInsuranceProccesor.class),
	MAZDA_INSURANCE(MazdaInsuranceProccesor.class),
	TELSTRA(TelstraProccesor.class),
	CAMRY_REGO(CamryRegoProcessor.class),
	MAZDA_REGO(MazdaRegoProcessor.class),
	WODONGA_RENT_STATEMENT(WodongaRentalStatementProccesor.class),
	DINGLEY_RATES(DingleyRatesProcessor.class);
	
	private Class processor;
	
	private EmailProcessor(Class processor) {
		this.processor = processor;
	}

	public Processor getProcessor() {
		return (Processor) SpringContext.getApplicationContext().getBean(processor);
	}

}
