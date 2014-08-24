package elmot.javabrick.ev3;

/**
* Created with IntelliJ IDEA.
* User: elmot
* Date: 03.08.14
* Time: 0:14
* To change this template use File | Settings | File Templates.
*/
public enum PORT {
    P1(0), P2(1), P3(2), P4(3);

    public final int portNum;

    PORT(int portNum) {
        this.portNum = portNum;
    }
}
