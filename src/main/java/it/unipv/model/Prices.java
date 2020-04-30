package it.unipv.model;

/**
 * Oggetto che rappresenta i prezzi del cinema.
 *     1) base -> è il prezzo di base del biglietto;
 *     2) vip -> è il supplemento per il vip;
 *     3) threed -> è il supplemento per una proiezione 3D;
 *     4) reduced -> è il prezzo ridotto per i disabili.
 */
public class Prices {
    private double base;
    private double vip;
    private double threed;
    private double reduced;

    public Prices(double base, double vip, double threed, double reduced) {
        this.base = base;
        this.vip = vip;
        this.threed = threed;
        this.reduced = reduced;
    }

    public void setBase(double base) { this.base = base; }

    public void setVip(double vip) { this.vip = vip; }

    public void setThreed(double threed) { this.threed = threed; }

    public void setReduced(double reduced) { this.reduced = reduced; }

    public double getBase() { return base; }

    public double getVip() { return vip; }

    public double getThreed() { return threed; }

    public double getReduced() { return reduced; }

    public String toString() {
        return   "Base: " + getBase() + "\n"
               + "Vip: " + getVip()+ "\n"
               + "3D: " + getThreed()+ "\n"
               + "Ridotto: " + getReduced()+ "\n";
    }

}
