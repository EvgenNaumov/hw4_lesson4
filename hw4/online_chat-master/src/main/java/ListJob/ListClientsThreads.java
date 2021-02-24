package ListJob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class ListClientsThreads {


    private Map<String, String> listThreads;

        public ListClientsThreads() {
            listThreads = new HashMap<>();
        }

        public void setListThreads(String nick) {
            String jobId = UUID.randomUUID().toString();
            listThreads.put(nick,jobId);
        }

    public void closeJob(String nick, ExecutorService ex) {
            if (listThreads.isEmpty()){
                return;
            }
        String idJob = listThreads.get(nick);

    }

    public void


}
