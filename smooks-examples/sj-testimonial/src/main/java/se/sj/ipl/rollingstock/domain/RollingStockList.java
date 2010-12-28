package se.sj.ipl.rollingstock.domain;

import java.util.ArrayList;
import java.util.List;

public class RollingStockList {
    
    private List<Rollingstock> rollingStocks = new ArrayList<Rollingstock>();

    public void add(Rollingstock rollingstock) {
        rollingStocks.add(rollingstock);
    }

    public int size() {
        return rollingStocks.size();
    }

    public Rollingstock get(int index) {
        return rollingStocks.get(index);
    }

    public String toString() {
        return rollingStocks.toString();
    }
}
