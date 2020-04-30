package it.unipv.model;

import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Questa classe rappresenta il singolo posto a sedere:
 * è una JLabel che presenta un bordo quadrato blu.
 * TODO: fare in modo che non possa essere trascinato all'esterno della piantina stessa
 */
public class Seat extends JLabel {

    private boolean amISelected;
    private boolean amICopied;
    private SeatTYPE type;

    public Seat(int x, int y, SeatTYPE type) {
        this.type = type;
        setBorder(new LineBorder(Color.BLUE, 3));
        setFont(this.getFont().deriveFont(9f));
        setBackground(Color.WHITE);
        setBounds(x, y, DataReferences.MYDRAGGABLESEATWIDTH, DataReferences.MYDRAGGABLESEATHEIGTH);
        setSize(DataReferences.MYDRAGGABLESEATWIDTH, DataReferences.MYDRAGGABLESEATHEIGTH);
        setOpaque(true);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setBackgroundPerType();
    }

    public void setIsSelected(boolean selected) { amISelected = selected; }

    public boolean getIsSelected() { return amISelected; }

    public boolean getIsCopied() { return amICopied; }

    public void setIsCopied(boolean amICopied) { this.amICopied = amICopied; }

    public SeatTYPE getType() { return type; }

    public void setType(SeatTYPE type) { this.type = type; }

    public void updateBackgroundForChangingType() { setBackgroundPerType(); }

    private void setBackgroundPerType() {
        switch (type) {
            case NORMALE:
                setBackground(new Color(0x9CED9F));
                break;

            case VIP:
                setBackground(new Color(0xE28FEF));
                break;

            case DISABILE:
                setBackground(new Color(0xc6c6c6));
                break;

            case OCCUPATO:
                setBackground(new Color(0xF97B84));
                break;

                default:
                    throw new ApplicationException("Type " + type.name() + " non riconosciuto!");
        }
    }
}