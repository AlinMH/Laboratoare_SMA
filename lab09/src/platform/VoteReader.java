package platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VoteReader {
	
	Map<String, VoteResult> voteResults = new java.util.HashMap<String, VoteResult>();
	
	public VoteReader() {
		readFile("data/region_votes.json");
	}
	
	private void readFile(String filename) {
		try {
            final InputStream in = new FileInputStream(new File(filename));
            ObjectMapper mapper =  new ObjectMapper();

            final JsonNode regionVotesNode = mapper.readTree(in);
            
            int id = 1;
            for (JsonNode regionVoteNode : regionVotesNode) {
                String regionName = "region" + (id++);
                VoteResult voteResult = mapper.treeToValue(regionVoteNode, VoteResult.class);
                
                voteResults.put(regionName, voteResult);
            }

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	
	public VoteResult getVoteResult(String regionName) {
		return voteResults.get(regionName);
	}
}

