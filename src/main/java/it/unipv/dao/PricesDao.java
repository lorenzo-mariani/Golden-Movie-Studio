package it.unipv.dao;

import it.unipv.model.Prices;

public interface PricesDao {
    Prices retrievePrices();
    void updatePrices(Prices p);
}
