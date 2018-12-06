package au.com.mason.expensemanager.processor;

import au.com.mason.expensemanager.config.SpringContext;

public enum EmailProcessor {
	DINGLEY_ELECTRICITY(DingleyElectricityProccesor.class),
	DINGLEY_GAS(DingleyGasProccesor.class),
	WODONGA_WATER(WodongaWaterProccesor.class),
	SOUTH_KINGSVILLE_RATES(SouthKingsvilleRatesProccesor.class),
	SOUTH_KINGSVILLE_WATER(SouthKingsvilleWaterProccesor.class),
	RACV_MEMBERSHIP(RACVMembershipProccesor.class),
	CAMRY_INSURANCE(CamryInsuranceProccesor.class),
	TELSTRA(TelstraProccesor.class),
	CAMRY_REGO(CamryRegoProcessor.class),
	MAZDA_REGO(MazdaRegoProcessor.class);
	
	private Class processor;
	
	private EmailProcessor(Class processor) {
		this.processor = processor;
	}

	public Processor getProcessor() {
		return (Processor) SpringContext.getApplicationContext().getBean(processor);
	}

}
