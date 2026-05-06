package com.codexp.iam.iam.application.queryservices;

import com.codexp.iam.iam.domain.model.entities.User;
import com.codexp.iam.iam.domain.model.queries.GetMyProfileQuery;
import com.codexp.iam.iam.domain.model.queries.GetPublicProfileQuery;
import com.codexp.iam.iam.domain.services.UserQueryService;
import com.codexp.iam.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getMyProfile(GetMyProfileQuery query) {
        return userRepository.findById(query.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public User getPublicProfile(GetPublicProfileQuery query) {
        return userRepository.findById(query.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
}
