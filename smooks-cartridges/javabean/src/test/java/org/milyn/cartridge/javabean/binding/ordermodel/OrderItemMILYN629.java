package org.milyn.cartridge.javabean.binding.ordermodel;

import java.util.List;

/**
 * See https://jira.codehaus.org/browse/MILYN-629
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class OrderItemMILYN629 extends OrderItem {


    private List<Money> money;

    public void setMoney(List<Money> costs) {
        this.money = costs;
    }

    public List<Money> getMoney() {
        return money;
    }
}
