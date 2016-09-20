package org.springframework.samples.petclinic.service;

import br.com.uol.pagseguro.domain.checkout.Checkout;
import br.com.uol.pagseguro.exception.PagSeguroServiceException;

public interface PagseguroService {
	
	String performCheckout(Checkout checkout) throws PagSeguroServiceException ;

}
