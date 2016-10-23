import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

// -------------------------------------------------------------------------
/**
 *  CMPE281HW2 Using command line to control the vm in vCenter
 *
 *  @author Sheng Zhou
 *  @version Oct 6, 2016
 */
public class CmpE281HW2
{

    // ----------------------------------------------------------
    /**
     * Main class to do the instructions
     * @param args contains three parameters: ip, loginname, and password
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("CMPE281 HW2 from Sheng Zhou");

        if (args.length != 3)
        {
            System.out.println("Please follow the instruction. Input IP "
                + "address, Login, and password.");
            System.exit(0);
        }
        URL ip = new URL("https://" + args[0] + "/sdk");
        ServiceInstance si = new ServiceInstance(ip, args[1], args[2], true);

        System.out.print("Sheng-677> ");
        BufferedReader read =
            new BufferedReader(new InputStreamReader(System.in));
        String usrInput = read.readLine();
        Folder rootFolder = si.getRootFolder();

        while (!"exit".equals(usrInput))
        {
            // usrInput is the command
            String[] command = usrInput.trim().split(" ");
            if (command.length == 1)
            {
                if (command[0].equals("help"))
                {
                    help();
                }
                if (command[0].equals("host"))
                {
                    host(rootFolder);
                }
                if (command[0].equals("vm"))
                {
                    vm(rootFolder);
                }
            }
            if (command.length == 3)
            {
                if (command[0].equals("host"))
                {
                    ManagedEntity[] hosts = new InventoryNavigator
                        (rootFolder).searchManagedEntities("HostSystem");
                    getHostInfo(hosts, command[1], command[2]);
                }
                if (command[0].equals("vm"))
                {
                    ManagedEntity[] vms = new InventoryNavigator
                        (rootFolder).searchManagedEntities("VirtualMachine");
                    getVMInfo(vms, command[1], command[2]);
                }
            }
            System.out.print("Sheng-677> ");
            usrInput = read.readLine();
        }
        si.getServerConnection().logout();
    }

    /**
     * The method is a help message when user types help.
     */
    private static void help()
    {
        System.out.println("usage:");
        System.out.println("exit                      exit the program");
        System.out.println("help                      print out the usage");
        System.out.println("host                      enumerate hosts");
        System.out.println("host hname info           show info for hname");
        System.out.println("host hname datastore      enumerate datastores for hname");
        System.out.println("host hname network        enumerate networks for hname");
        System.out.println("vm                        enumerate vms");
        System.out.println("vm vname info             show info for vname");
        System.out.println("vm vname shutdown         shutdown OS on vname");
        System.out.println("vm vname on               power on vname");
        System.out.println("vm vname off              power off vname");
    }

    /**
     * Enumerate all hosts
     * @param rootFolder root folder name
     * @throws Exception
     */
    private static void host(Folder rootFolder) throws Exception
    {
        // Enumerate all hosts
        int hostIdx = 0;
        ManagedEntity[] hosts = new InventoryNavigator
            (rootFolder).searchManagedEntities("HostSystem");
        if (hosts == null || hosts.length == 0)
        {
            return;
        }
        for (ManagedEntity hostObj: hosts)
        {
            System.out.print("host[" + hostIdx + "]: Name = ");
            HostSystem host = (HostSystem) hostObj;
            // print host name
            System.out.print(host.getName() + "\n");
            hostIdx++;
        }
    }

    /**
     * Enumerate all virtual machines.
     * @param rootFolder the folder of root
     * @throws Exception
     */
    private static void vm(Folder rootFolder) throws Exception
    {
        int vmIdx = 0;
        ManagedEntity[] mes = new InventoryNavigator
            (rootFolder).searchManagedEntities("VirtualMachine");
        if (mes == null || mes.length == 0)
        {
            return;
        }
        for (ManagedEntity me: mes)
        {
            System.out.print("VM[" + vmIdx + "]: name = ");

            // enumerate all vms
            VirtualMachine vm = (VirtualMachine) me;
            // print vm names
            System.out.print(vm.getName() + "\n");
            vmIdx++;
        }
    }

    /**
     * Place a description of your method here.
     * @param hosts hosts entities
     * @param hname user input host name
     * @param cmd user input command
     * @throws Exception
     */
    private static void getHostInfo(ManagedEntity[] hosts, String hname, String cmd) throws Exception
    {
        Boolean validHname = false;
        int dsIdx = 0;
        int netIdx = 0;
        if (hosts == null || hosts.length == 0)
        {
            return;
        }
        for (ManagedEntity hostObj: hosts)
        {
            HostSystem host = (HostSystem) hostObj;
            if (host.getName().equals(hname))
            {
                validHname = true;
                System.out.println("Host: ");
                System.out.println("\tName = " + host.getName());

                if (cmd.equals("info"))
                {
                    // print host infomation
                    System.out.println("\tProductFullName = " +
                        host.getConfig().getProduct().getFullName());
                    System.out.println("\tCpu cores = " +
                        host.getSummary().getHardware().numCpuCores);
                    System.out.println("\tRAM = " +
                        host.getSummary().getHardware().memorySize/1073741824
                        +" GB");
                }
                if (cmd.equals("datastore"))
                {
                    // Enumerate all Datastores
                    Datastore[] datastores = host.getDatastores();
                    for (ManagedEntity datastore: datastores)
                    {
                        Datastore ds = (Datastore)datastore;
                        // print name, capacity and available space
                        System.out.print("\tDatastore[" + dsIdx + "]: name=" +
                            ds.getInfo().getName() + ", capacity = " +
                            ds.getSummary().getCapacity()/1073741824 +
                            " GB, FreeSpace = " +
                            ds.getSummary().getFreeSpace()/1073741824 + " GB\n");
                        dsIdx++;
                    }
                }
                if (cmd.equals("network"))
                {
                    // Enumerate all Networks
                    Network[] networks = host.getNetworks();
                    for (Network network: networks)
                    {
                        System.out.print("\tNetwork[" + netIdx + "]: name=" +
                            network.getName() + "\n");
                        netIdx++;
                    }
                }
            }
        }
        if (!validHname)
        {
            System.out.println("invalid host name = " + hname);
        }
    }

    /**
     * The method is to get virtual machine infos and get info w/ user requests
     * @param hosts hosts entities
     * @param vmname virtual machine name
     * @param cmd user input command
     * @throws Exception
     */
    private static void getVMInfo(ManagedEntity[] vms, String vmname, String cmd) throws RemoteException, Exception
    {
        Boolean validVMname = false;
        String pattern = "MM/dd/yyyy HH:mm:ss";
        if (vms == null || vms.length == 0)
        {
            return;
        }
        for (ManagedEntity vmOBJ: vms)
        {
            VirtualMachine vm = (VirtualMachine) vmOBJ;
            if (vm.getName().equals(vmname))
            {
                validVMname = true;
                System.out.println("Virtual Machine: ");
                System.out.println("\tName = " + vm.getName());

                if (cmd.equals("info"))
                {
                    // print vm infomation
                    System.out.println("\tGuest full name = " +
                        vm.getConfig().getGuestFullName());
                    System.out.println("\tGuest State = " +
                        vm.getGuest().getGuestState());
                    System.out.println("\tIP addr = " +
                        vm.getGuest().ipAddress);
                    System.out.println("\tTool running status = " +
                        vm.getGuest().toolsRunningStatus);
                    // Create variable for reuse
                    VirtualMachinePowerState pState = vm.getRuntime()
                        .getPowerState();
                    System.out.println("\tPower State = " + pState);
                }
                if (cmd.equals("on"))
                {
                    Task task = vm.powerOnVM_Task(null);
                    task.waitForTask();
                    System.out.print("\tPower on VM: status = ");
                    if (task.getTaskInfo().getState().toString()
                            .equals("success"))
                    {
                        System.out.print(task.getTaskInfo().getState());
                    }
                    else
                    {
                        System.out.print(task.getTaskInfo().getError()
                            .getLocalizedMessage());
                    }
                    SimpleDateFormat format = new SimpleDateFormat(pattern);
                    System.out.println(", completion time = "
                        + format.format(task.getTaskInfo().getCompleteTime()
                            .getTime()));

                }
                if (cmd.equals("off"))
                {
                    Task task = vm.powerOffVM_Task();
                    task.waitForTask();
                    System.out.print("\tPower off VM: status = ");
                    if (task.getTaskInfo().getState().toString()
                            .equals("success"))
                    {
                        System.out.print(task.getTaskInfo().getState());
                    }
                    else
                    {
                        System.out.print(task.getTaskInfo().getError()
                            .getLocalizedMessage());
                    }
                    SimpleDateFormat format = new SimpleDateFormat(pattern);
                    System.out.println(", completion time = "
                        + format.format(task.getTaskInfo().getCompleteTime()
                            .getTime()));
                }
                if (cmd.equals("shutdown"))
                {
                    // could shut down iff validpowerstate
                    long startTime = System.currentTimeMillis();
                    Timer timer = new Timer();
                    timer.schedule(new CheckPowerState(vm), 2000, 2000);

                    try
                    {
                        if (vm.getRuntime().getPowerState().toString()
                                .equals("poweredOff"))
                        {
                            throw new RemoteException();
                        }
                        while ((System.currentTimeMillis()-startTime) < 180000)
                        {
                            if (vm.getRuntime().getPowerState().toString().
                                    equals("poweredOn"))
                            {
                                {
                                    vm.shutdownGuest();
                                    System.out.print("\tShutdown guest:");
                                    System.out.print("completed, time = ");

                                    timer.cancel();
                                    timer.purge();

                                    Task[] recents = vm.getRecentTasks();
                                    Task mostRecent = recents[recents.length-1];
                                    SimpleDateFormat format =
                                        new SimpleDateFormat(pattern);
                                    System.out.println(format.format
                                        (mostRecent.getTaskInfo().
                                            getCompleteTime().getTime()));
                                    break;
                                }
                            }
                        }
                        if ((System.currentTimeMillis()-startTime) > 180000 &&
                                vm.getRuntime().getPowerState().toString()
                                .equals("poweredOn"))
                        {
                            throw new RemoteException();
                        }
                    }
                    catch (RemoteException ex)
                    {
                        System.out.println("\tGraceful shutdown failed. "
                            + "Now try a hard power off.");
                        Task task = vm.powerOffVM_Task();
                        task.waitForTask();
                        timer.cancel();
                        timer.purge();
                        System.out.print("\tPower off VM: status = ");
                        if (task.getTaskInfo().getState().toString()
                            .equals("success"))
                        {
                            System.out.print(task.getTaskInfo().getState());
                        }
                        else
                        {
                            System.out.print(task.getTaskInfo().getError()
                                .getLocalizedMessage());
                        }
                        SimpleDateFormat format = new SimpleDateFormat(pattern);
                        System.out.println(", completion time = "
                            + format.format(task.getTaskInfo().getCompleteTime()
                                .getTime()));
                    }

                    /*
                    if (vm.getRuntime().getPowerState().toString().equals("poweredOn"))
                    {
                        // check if the vm runs the vmware tools
                        if (vm.getGuest().getGuestFullName() != null)
                        {
                            vm.shutdownGuest();
                            System.out.print("\tShutdown guest:");
                            System.out.print("completed, time = ");
                            Task[] recents = vm.getRecentTasks();
                            SimpleDateFormat format = new SimpleDateFormat(pattern);
                            System.out.println(format.format(recents[recents.length-1].getTaskInfo().
                                getCompleteTime().getTime()));
                            timer.cancel();
                            timer.purge();
                        }
                        else
                        {
                            System.out.println("\tGraceful shutdown failed. "
                                    + "Now try a hard power off.");
                            Task task = vm.powerOffVM_Task();
                            task.waitForTask();
                            timer.cancel();
                            timer.purge();
                            System.out.print("\tPower off VM: status = ");
                            if (task.getTaskInfo().getState().toString().equals("success"))
                            {
                                System.out.print(task.getTaskInfo().getState());
                            }
                            else
                            {
                                System.out.print(task.getTaskInfo().getError().getLocalizedMessage());
                            }
                            SimpleDateFormat format = new SimpleDateFormat(pattern);
                            System.out.println(", completion time = "
                                + format.format(task.getTaskInfo().getCompleteTime().getTime()));
                        }
                    }*/
                }
            }
        }
        if (!validVMname)
        {
            System.out.println("invalid vm name = " + vmname);
        }
    }
}

// -------------------------------------------------------------------------
/**
 *  The timer for pulling virtual machine's power state for every 2 second
 *  after getting user command: shutdown.
 *
 *  @author Sheng Zhou
 *  @version Oct 9, 2016
 */
class CheckPowerState extends TimerTask
{
    private final VirtualMachine vm;

    // ----------------------------------------------------------
    /**
     * Create a new CheckPowerState object.
     * @param vm virtualmachine
     */
    CheckPowerState(VirtualMachine vm)
    {
        this.vm = vm;
    }

    public void run() {
       if (vm.getRuntime().getPowerState().toString().equals("poweredOn"))
       {
           System.out.println("\tCurrent Power State = " +
               vm.getRuntime().getPowerState());
       }
    }
}