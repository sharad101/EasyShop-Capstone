package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;

import java.security.Principal;


//Phase 4 Optional
@RestController
@RequestMapping("/profile")
@CrossOrigin
// Only logged-in users can access
@PreAuthorize("isAuthenticated()")
public class ProfileController
{
    private ProfileDao profileDao;
    private UserDao userDao;

    public ProfileController(ProfileDao profileDao, UserDao userDao)
    {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    // GET /profile
    // NEW: Retrieves the profile for the currently logged-in user
    @GetMapping
    public Profile getProfile(Principal principal)
    {
        try
        {
            // get logged-in username
            String username = principal.getName();
            User user = userDao.getByUserName(username);

            Profile profile = profileDao.getByUserId(user.getId());
            if (profile == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found.");
            }

            return profile;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving profile.");
        }
    }

    // PUT /profile
    // NEW: Updates the profile data for the logged-in user
    @PutMapping
    public void updateProfile(@RequestBody Profile profile, Principal principal)
    {
        try
        {
            String username = principal.getName();
            User user = userDao.getByUserName(username);

            // set userId on profile
            profile.setUserId(user.getId());
            // update profile in DB
            profileDao.update(profile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating profile.");
        }
    }
}
