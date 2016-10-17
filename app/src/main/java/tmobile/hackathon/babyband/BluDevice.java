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
        this.addr = addr;
    }

    public String getAddr()
    {
        return addr;
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
                return "Williams Iphone";
            default:
                return "null";
        }
    }

}
