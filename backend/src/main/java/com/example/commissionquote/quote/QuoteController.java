package com.example.commissionquote.quote;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.commissionquote.common.AppConstants.Paths;

/**
 * HTTP entry point for commission quotes. Only handles HTTP concerns; errors are turned into responses by
 * {@link com.example.commissionquote.common.error.ApiExceptionHandler}.
 */
@RestController
@RequestMapping(Paths.QUOTES)
public class QuoteController {

	private final QuoteService quoteService;

	public QuoteController(QuoteService quoteService) {
		this.quoteService = quoteService;
	}

	@PostMapping
	public QuoteResponse createQuote(@Valid @RequestBody QuoteRequest request) {
		return quoteService.generateQuote(request);
	}
}
