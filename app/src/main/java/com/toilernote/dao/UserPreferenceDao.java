package com.toilernote.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.toilernote.entity.UserPreference;

@Dao
public interface UserPreferenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserPreference preference);

    @Update
    void update(UserPreference preference);

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    UserPreference getPreference();

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    LiveData<UserPreference> getPreferenceLive();
}
