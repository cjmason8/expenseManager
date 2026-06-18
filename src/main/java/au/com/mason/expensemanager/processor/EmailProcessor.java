package au.com.mason.expensemanager.processor;

import au.com.mason.expensemanager.config.SpringContext;

public enum EmailProcessor {
	DINGLEY_ELECTRICITY(DingleyElectricityProcessor.class), DINGLEY_GAS(DingleyGasProcessor.class), DINGLEY_WATER(
		DingleyWaterProcessor.class), WODONGA_WATER(WodongaWaterProcessor.class), WODONGA_RATES(
			WodongaRatesProcessor.class), WODONGA_INSURANCE(WodongaInsuranceProcessor.class), DINGLEY_INSURANCE(
				DingleyInsuranceProcessor.class), SOUTH_KINGSVILLE_RATES(
					SouthKingsvilleRatesProcessor.class), SOUTH_KINGSVILLE_WATER(
						SouthKingsvilleWaterProcessor.class), SOUTH_KINGSVILLE_INSURANCE(
							SouthKingsvilleInsuranceProcessor.class), RACV_MEMBERSHIP(
								RACVMembershipProcessor.class), CAMRY_INSURANCE(
									CamryInsuranceProcessor.class), MAZDA_INSURANCE(
										MazdaInsuranceProcessor.class), FORD_INSURANCE(
											FordInsuranceProcessor.class), TELSTRA(TelstraProcessor.class), CAMRY_REGO(
												CamryRegoProcessor.class), MAZDA_REGO(
													MazdaRegoProcessor.class), WODONGA_RENT_STATEMENT(
														WodongaRentalStatementProcessor.class), DINGLEY_RATES(
															DingleyRatesProcessor.class), VERADA_REGO(
																VeradaRegoProcessor.class), FORESTER_REGO(
																	ForesterRegoProcessor.class), FORESTER_INSURANCE(
																		ForesterInsuranceProcessor.class), SOUTH_KINGSVILLE_RENT_STATEMENT(
																			SouthKingsvilleRentalStatementProcessor.class), GLOBIRD_ELECTRICITY_AND_GAS(
																				GlobirdElectricityAndGasProcessor.class);

	private Class processor;

	private EmailProcessor(Class processor) {
		this.processor = processor;
	}

	public Processor getProcessor() {
		return (Processor) SpringContext.getApplicationContext().getBean(processor);
	}

}
