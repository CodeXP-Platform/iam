package com.codexp.iam.iam.domain.services;

import com.codexp.iam.iam.domain.model.entities.User;
import com.codexp.iam.iam.domain.model.queries.GetMyProfileQuery;
import com.codexp.iam.iam.domain.model.queries.GetPublicProfileQuery;

/**
 * Pure domain service interface for read operations.
 * No Spring annotations — dependency inversion boundary.
 */
public interface UserQueryService {

    User getMyProfile(GetMyProfileQuery query);

    User getPublicProfile(GetPublicProfileQuery query);
}
