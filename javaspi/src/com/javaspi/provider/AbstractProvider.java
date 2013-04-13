package com.javaspi.provider;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractProvider {

	private static Map<String, Provider> providers = new HashMap<String, Provider>();
	
	public static Provider getInstance(String context) {
		if(providers.get(context) == null) {
			providers.put(context, createProvider(context));
		}
		return providers.get(context);
	}
	
	private static Provider createProvider(String context) {
		return new DefaultProvider(context);
	}
}
