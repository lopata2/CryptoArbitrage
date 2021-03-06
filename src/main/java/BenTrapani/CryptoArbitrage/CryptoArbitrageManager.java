package BenTrapani.CryptoArbitrage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.ProductSubscription.ProductSubscriptionBuilder;
import io.reactivex.disposables.Disposable;

public class CryptoArbitrageManager {
	private ArrayList<Disposable> subscriptions;
	private StreamingExchangeSubset[] exchanges;
	private OrderGraph orderGraph = new OrderGraph();
	private ArbitrageExecutor arbitrageExecutor = new ArbitrageExecutor(new Fraction(1));
	private OrderBookAnalyzer orderBookAnalyzer = new OrderBookAnalyzer(orderGraph, Currency.BTC, 4, arbitrageExecutor);
	private OrderBookAggregator orderBookAggregator = new OrderBookAggregator(orderGraph, orderBookAnalyzer, 1, 1);

	public CryptoArbitrageManager(StreamingExchangeSubset[] exchanges) {
		subscriptions = new ArrayList<Disposable>(exchanges.length);
		for (StreamingExchangeSubset exchange : exchanges) {
			Set<CurrencyPair> currenciesForExchange = exchange.getCurrencyPairs();
			ProductSubscriptionBuilder builder = ProductSubscription.create();
			for (CurrencyPair currencyPair : currenciesForExchange) {
				builder = builder.addOrderbook(currencyPair);
			}
			exchange.buildAndWait(builder);
		}
		this.exchanges = exchanges.clone();
		arbitrageExecutor.setExchanges(this.exchanges);
	}

	public void startArbitrage() {
		for (int i = 0; i < exchanges.length; i++) {
			Disposable[] tempDisposables = orderBookAggregator.createConsumerForExchange(exchanges[i]);
			List<Disposable> subsList = new ArrayList<Disposable>(Arrays.asList(tempDisposables));
			subscriptions.addAll(subsList);
		}
		orderBookAnalyzer.startAnalyzingOrderBook();
	}

	public void stopArbitrage() {
		for (Disposable disp : subscriptions) {
			try {
				disp.dispose();
			} catch (Exception e) {
				System.out.println("Error disposing order book subscriber: " + e.toString());
			}
		}
		try {
			orderBookAnalyzer.stopAnalyzingOrderBook();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
