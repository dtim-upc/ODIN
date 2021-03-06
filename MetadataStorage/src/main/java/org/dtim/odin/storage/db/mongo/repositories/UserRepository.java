package org.dtim.odin.storage.db.mongo.repositories;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.dtim.odin.storage.db.mongo.MongoConnection;
import org.dtim.odin.storage.db.mongo.models.UserModel;
import org.dtim.odin.storage.db.mongo.utils.UtilsMongo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class UserRepository {

    private final String FIELD_USERNAME = "username";
    private MongoCollection<UserModel> usersCollection;

    public UserRepository() {
        usersCollection = MongoConnection.getInstance().getDatabase().getCollection("users", UserModel.class);
    }


    public UserModel findByUsername(String username) {
       return usersCollection.find(eq(FIELD_USERNAME,username)).first();
    }

    public boolean exist(String username){
        if(findByUsername(username) != null)
            return true;
        return false;
    }


    public void create(String userJson){
        try {
            UserModel user = UtilsMongo.mapper.readValue(userJson, UserModel.class);
            usersCollection.insertOne(user);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (com.mongodb.MongoWriteException ex){
            //TODO:  Handle error when not able to write in db and check other exception throw by insertOne
        }
    }

    public List<UserModel> findAll(){
        List<UserModel> users = new ArrayList();
        MongoCursor cur = usersCollection.find().iterator();
        while(cur.hasNext()) {
            users.add((UserModel)cur.next());
        }
        return users;
    }

}
