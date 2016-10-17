package tmobile.hackathon.babyband;

/**
 * Created by William Zulueta on 10/16/16.
 */

public class BluDevice
{
    private String addr;
    private int amount;

    public BluDevice(String a)
    {
        this.addr = a;
    }

    public String getAddr()
    {
        return addr;
    }

    public void reset()
    {
        amount = 0;
    }

    public int getAmount()
    {
        return amount;
    }

    public void increaseAmount()
    {
        ++amount;
    }

    public String getName()
    {
        switch (addr)
        {
            case "577ae0fc-ca17-37f4-8ec5-5884a5941d0f":
                return "Stacey's Model S";
            case "a85568e7-e011-3134-bbba-0a564f8130ea":
                return "Dad's 6P";
            default:
                return "null";
        }
    }

}
