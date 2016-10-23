import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

// -------------------------------------------------------------------------
/**
 *  The HW for cmpe283 ESXi,
 *  try to using java code to grab info or control the vms.
 *
 *  @author Sheng Zhou
 *  @version Oct 5, 2016
 */
public class CmpE283Q2
{
    // ----------------------------------------------------------
    /**
     * Main functions with 2 steps
     * @param args ip, root, and password
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length != 3)
        {
            System.out.println("Please follow the instruction. Input IP "
                + "address, Login, and password.");
            System.exit(0);
        }

        int hostIdx = 0;
        int vmIdx = 0;
        int dsIdx = 0;
        int netIdx = 0;
        String pattern = "MM/dd/yyyy HH:mm:ss";

        System.out.println("CMPE283 HW1 from Sheng Zhou");
        URL ip = new URL("https://" + args[0] + "/sdk");
        ServiceInstance si = new ServiceInstance(ip, args[1], args[2], true);
        Folder rootFolder = si.getRootFolder();
        //Step1: Enumerate all hosts
        ManagedEntity[] hosts = new InventoryNavigator
            (rootFolder).searchManagedEntities("HostSystem");
        if (hosts == null || hosts.length == 0)
        {
            return;
        }
        for (ManagedEntity hostObj: hosts)
        {
            System.out.println("host[" + hostIdx + "]:");

            HostSystem host = (HostSystem) hostObj;
            // print host name
            System.out.println("Name = " + host.getName());
            // print product full name
            System.out.println("ProductFullName = " + host.getConfig().
                getProduct().getFullName());

            // Enumerate all Datastores
            Datastore[] datastores = host.getDatastores();
            for (ManagedEntity datastore: datastores)
            {
                Datastore ds = (Datastore)datastore;
                // print name, capacity and available space
                System.out.print("Datastore[" + dsIdx + "]: name=" +
                    ds.getInfo().getName() + ", capacity = " +
                    ds.getSummary().getCapacity() + ", FreeSpace = "
                    + ds.getSummary().getFreeSpace() + "\n");
                dsIdx++;
            }

            // Enumerate all Networks
            Network[] networks = host.getNetworks();
            for (Network network: networks)
            {
                System.out.print("Network[" + netIdx + "]: name=" +
                    network.getName() + "\n");
                netIdx++;
            }

            hostIdx++;
            System.out.println();
        }

        //Step2: Enumerate all VMs
        ManagedEntity[] mes = new InventoryNavigator
            (rootFolder).searchManagedEntities("VirtualMachine");
        if (mes == null || mes.length == 0)
        {
            return;
        }
        for (ManagedEntity me: mes)
        {
            System.out.println("VM[" + vmIdx + "]:");

            // print VM name, guest OS full name, guest state, and power state
            VirtualMachine vm = (VirtualMachine) me;

            System.out.println("Name = " + vm.getName());
            System.out.println("GuestOS = " + vm.getConfig().getGuestFullName());
            System.out.println("Guest State = " + vm.getGuest().getGuestState());

            // Create variable for reuse
            VirtualMachinePowerState pState = vm.getRuntime().getPowerState();
            System.out.println("Power State = " + pState);

            // power on off-vms and power off on-vms
            // better to use VirtualMachinePowerState.poweredOff
            if (pState.toString().equals("poweredOff"))
            {
                Task task = vm.powerOnVM_Task((HostSystem)hosts[0]);
                task.waitForTask();
                System.out.print("Power on VM: status = ");
                if (task.getTaskInfo().getState().toString().equals("success"))
                {
                    System.out.println(task.getTaskInfo().getState());
                }
                else
                {
                    System.out.println(task.getTaskInfo().getError().getLocalizedMessage());
                }
            }
            else if (pState.toString().equals("poweredOn"))
            {
                Task task = vm.powerOffVM_Task();
                task.waitForTask();
                System.out.print("Power off VM: status = ");
                if (task.getTaskInfo().getState().toString().equals("success"))
                {
                    System.out.println(task.getTaskInfo().getState());
                }
                else
                {
                    System.out.println(task.getTaskInfo().getError().getLocalizedMessage());
                }
            }

            // retrieve the recent tasks
            Task[] recents = vm.getRecentTasks();
            for (Task recent : recents)
            {
                System.out.print("task: ");
                // print target, operation name, and start time
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                System.out.print("target=" + vm.getName() + ", op=" +
                    recent.getTaskInfo().getName() + ", startTime=" +
                    format.format(recent.getTaskInfo().getStartTime().getTime())
                    + "\n");
            }

            vmIdx++;
            System.out.println();
        }
        si.getServerConnection().logout();
    }
}
