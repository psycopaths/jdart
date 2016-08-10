/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package features.tableswitch;

/**
 *
 * @author falk
 */
public class Input {
    
    public void tblSwitch(int i) {        
        switch (i) {
            case 1: 
            case 2:
            case 3: assert i < 3; break;
            case 4: assert false;
            default: assert true;
        }
    }
    
    public static void main(String[] args) {
        Input i = new Input();
        i.tblSwitch(2);
    }
}
