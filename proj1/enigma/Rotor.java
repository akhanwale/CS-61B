package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Aniruddh Khanwale
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        this.set(0);
        this._stellung = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Set this rotors permutation.
     * @param perm the permutation to set this to */
    void setPermutation(Permutation perm) {
        _permutation = perm;
    }
    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Set the Ringstellung value.
     * @param stellung the value to which stellung should be set */
    void setStellung(char stellung) {
        _stellung = this.permutation().alphabet().toInt(stellung);
    }
    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = posn;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        this.set(this.alphabet().toInt(cposn));
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int enterAlias = this.permutation().wrap(
                p - _stellung + this.setting());
        int convert = this.permutation().permute(enterAlias);
        return this.permutation().wrap(convert + _stellung - this.setting());
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int enterAlias = this.permutation().wrap(
                e - _stellung + this.setting());
        int convert = this.permutation().invert(enterAlias);
        return this.permutation().wrap(convert + _stellung - this.setting());
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** The current setting of this rotor. **/
    private int _setting = 0;

    /** The Ringstellung. */
    private int _stellung = 0;

}
