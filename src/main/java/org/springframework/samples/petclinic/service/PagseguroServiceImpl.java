package org.springframework.samples.petclinic.service;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import br.com.uol.pagseguro.domain.checkout.Checkout;
import br.com.uol.pagseguro.exception.PagSeguroServiceException;
import br.com.uol.pagseguro.properties.PagSeguroConfig;

@Service
public class PagseguroServiceImpl implements PagseguroService {
	
	@Override
	public String performCheckout(final Checkout checkout) throws PagSeguroServiceException {
		Assert.notNull(checkout);
		
		return checkout.register(PagSeguroConfig.getAccountCredentials(), false);
	}

}
