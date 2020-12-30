package it.eng.idsa.businesslogic.util;

public enum IDSHeaders {
	
	
	IDS_MODEL_VERSION("ids-modelVersion"),
	IDS_SENDER_AGENT("ids-senderAgent"),
	IDS_RECIPIENT_AGENT("ids-recipientAgent");
	
	
	
	private final String name;
	 
    private IDSHeaders(String name) {
        this.name = name;
    }

	public String getName() {
		return name;
	}
    
    

}
