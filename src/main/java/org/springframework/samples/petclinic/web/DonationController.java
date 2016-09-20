/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import java.math.BigDecimal;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.PagseguroService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.uol.pagseguro.domain.checkout.Checkout;
import br.com.uol.pagseguro.enums.Currency;
import br.com.uol.pagseguro.enums.DocumentType;
import br.com.uol.pagseguro.enums.ShippingType;
import br.com.uol.pagseguro.exception.PagSeguroServiceException;

/**
 * @author gnagib
 */
@Controller
public class DonationController {

    private final ClinicService clinicService;
    
    private final PagseguroService pagseguroService;


    @Autowired
    public DonationController(ClinicService clinicService, PagseguroService pagseguroService) {
        this.clinicService = clinicService;
        this.pagseguroService = pagseguroService;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    /**
     * Called before each and every @RequestMapping annotated method.
     * 2 goals:
     * - Make sure we always have fresh data
     * - Since we do not use the session scope, make sure that Pet object always has an id
     * (Even though id is not part of the form fields)
     *
     * @param petId
     * @return Pet
     */
    @ModelAttribute("checkout")
    public Checkout createPetCheckout(@PathVariable("petId") int petId) {
        final Pet pet = this.clinicService.findPetById(petId);
        final Owner owner = pet.getOwner();
        
        final Checkout checkout = new Checkout();

        checkout.addItem("0001", //
                pet.getName(), //
                Integer.valueOf(1), //
                new BigDecimal("2430.00"), //
                1L, 
                null);
        
        checkout.setShippingAddress("BRA", //
                "SP", //
                owner.getCity(), //
                "Jardim Paulistano", //
                "01452002", //
                owner.getAddress(), //
                "1384", //
                "5o andar");

        checkout.setShippingType(ShippingType.SEDEX);

        checkout.setShippingCost(new BigDecimal("2.02"));

        checkout.setSender(String.format("%s %s", owner.getFirstName(), owner.getLastName()), //
                "petstore@uol.com.br", //
                "11", //
                owner.getTelephone(), //
                DocumentType.CPF, //
                "000.000.001-91");

        checkout.setCurrency(Currency.BRL);

        // Sets a reference code for this payment request, it's useful to
        // identify this payment in future notifications
        checkout.setReference("REF1234");
        
        checkout.setRedirectURL(String.format("http://localhost:9966/petclinic/owners/%s.html", owner.getId()));
        
        return checkout;
    }

    // Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
    @RequestMapping(value = "/owners/{ownerId}/pets/{petId}/donate/pay", method = RequestMethod.GET)
    public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model, @Valid Checkout checkout) {
    	try {
			final String psUrl = pagseguroService.performCheckout(checkout);
			return "redirect:"+psUrl;
		} catch (PagSeguroServiceException e) {
			e.printStackTrace();
			return "redirect:/owners/{ownerId}";
		}
    }

}
