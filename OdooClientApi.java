package com.gii.midapp.xmlrpc;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;

@Component
public class OdooClientApi {
    private final String url;
    private final String username;
    private final String db;
    private final String password;
    private int uid;
    private XmlRpcClientConfigImpl commonConfig; // Global variable for common server configuration
    private XmlRpcClientConfigImpl modelsConfig; // Global variable for models server configuration

    /**
     * Constructor
     *
     * @param url       URL of the Odoo server
     * @param db        Database name
     * @param username  Username
     * @param password  Password
     */
    @Autowired
    public OdooClientApi(@Value("${odoo.datasource.url}") String url,
                         @Value("${odoo.datasource.db}") String db,
                         @Value("${odoo.datasource.username}") String username,
                         @Value("${odoo.datasource.password}") String password) {
        this.db = db;
        this.username = username;
        this.password = password;
        this.url = url;
        this.commonConfig = createCommonConfig(); // Create the common server configuration
        this.modelsConfig = createModelsConfig(); // Create the models server configuration
        authenticate();
    }

    /**
     * Create the common server configuration
     *
     * @return config  The common server configuration
     */
    private XmlRpcClientConfigImpl createCommonConfig() {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(url + "/xmlrpc/2/common"));
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Error while creating common config", e);
        }
    }

    /**
     * Create the models server configuration
     *
     * @return config  The models server configuration
     */
    private XmlRpcClientConfigImpl createModelsConfig() {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(url + "/xmlrpc/2/object"));
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Error while creating models config", e);
        }
    }

    /**
     * Authenticate to the Odoo server
     * Description: Authenticate to the Odoo server is required to perform any operation
     * this method is purposed to obtain the uid of the user
     */
    private void authenticate() {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(commonConfig);

            Object[] authParams = new Object[]{db, username, password, new HashMap<>()};
            uid = (int) client.execute("authenticate", authParams);
            System.out.println("UID: " + uid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Search and read records
     *
     * @param model         Model name
     * @param searchParams  Search parameters
     * @return records      List of records
     */
    public List<Object> searchAndRead(String model, List<Object> searchParams) {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(modelsConfig);

            return asList((Object[]) client.execute("execute_kw", asList(
                    db, uid, password,
                    model, "search_read",
                    searchParams
            )));
        } catch (XmlRpcException xe) {
            System.out.println("Error: " + xe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Search Count
     *
     * @param model         Model name
     * @param searchParams  Search parameters
     * @return count        Number of records
     */
    public Integer searchCount(String model, List<Object> searchParams) {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(modelsConfig);

            return (Integer) client.execute("execute_kw", asList(
                    db, uid, password,
                    model, "search_count",
                    searchParams
            ));
        } catch (XmlRpcException xe) {
            System.out.println("Error: " + xe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a record
     *
     * @param model         Model name
     * @param data          Data to be created
     * @param file          File to be uploaded
     * @return id           the id of the created record
     */
    public Integer create(String model, Map<String, Object> data, MultipartFile file) {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(modelsConfig);

            // Create a param for the data
            List<Object> params = asList(
                    data
            );

            // Check if the file is not null
            if (file != null) {
                byte[] fileBytes = file.getBytes();
                String encodedFile = Base64.getEncoder().encodeToString(fileBytes);
                data.put("image_1920", encodedFile);
            }

            // Create a record
            return (Integer) client.execute("execute_kw", asList(
                    db, uid, password,
                    model, "create",
                    params
            ));
        } catch (XmlRpcException xe) {
            System.out.println("Error: " + xe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update a record
     *
     * @param model         Model name
     * @param id            Id of the record to be updated
     * @param data          Data to be updated
     * @param file          File to be uploaded
     * @return Object[]     the updated record (Keeping it original type)
     */
    public Object[] update(String model, Integer id, Map<String, Object> data, MultipartFile file) {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(modelsConfig);

            // create a param
            List<Object> params = asList(
                    asList(id),
                    data
            );

            // check if file is not null
            if (file != null) {
                byte[] fileBytes = file.getBytes();
                String encodedFile = Base64.getEncoder().encodeToString(fileBytes);
                data.put("image_1920", encodedFile);
            }

            // update a record
            client.execute("execute_kw", asList(
                    db, uid, password,
                    model, "write",
                    params
            ));

            // get record name after having changed it
            return (Object[]) client.execute("execute_kw", asList(
                    db, uid, password,
                    model, "name_get",
                    asList(asList(id))
            ));
        } catch (XmlRpcException xe) {
            System.out.println("Error: " + xe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete a record
     *
     * @param model     Model name
     * @param id        Id of the record to be deleted
     * @return message  Message of the operation
     */
    public String delete(String model, Integer id) {
        try {
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(modelsConfig);

            // delete a record
            client.execute("execute_kw", asList(
                    db, uid, password,
                    model, "unlink",
                    asList(asList(id))
            ));
            return "Record with id: " + id + " has been deleted";
        } catch (XmlRpcException xe) {
            return xe.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}