package org.yearup.data;


import org.yearup.models.Profile;

public interface ProfileDao
{
    Profile create(Profile profile);

    // Phase 4 New
    // NEW: Get a profile by the user’s ID (used when viewing profile)
    Profile getByUserId(int userId);

    //Phase 4 New
    // NEW: Update a profile for a given user ID (used when editing profile)
    void update(Profile profile);
}
