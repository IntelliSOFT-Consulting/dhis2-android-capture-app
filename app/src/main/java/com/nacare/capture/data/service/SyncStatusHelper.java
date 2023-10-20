package com.nacare.capture.data.service;


import static org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope.OrderByDirection.DESC;

import android.os.Build;

import com.nacare.capture.models.EventWithOrganization;
import com.nacare.capture.data.Sdk;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramSection;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SyncStatusHelper {

    public static int programCount() {
        return Sdk.d2().programModule().programs().blockingCount();
    }

    public static List<Program> programList() {
        return Sdk.d2().programModule()
                .programs()
                .get()
                .blockingGet();
    }


    public static Program singleProgram(String programUid) {

        return Sdk.d2().programModule().programs()
                .byUid().eq(programUid)
                .one()
                .blockingGet();
    }

    public static int dataSetCount() {
        return Sdk.d2().dataSetModule().dataSets().blockingCount();
    }

    public static int trackedEntityInstanceCount() {
        return Sdk.d2().trackedEntityModule().trackedEntityInstances()
                .byAggregatedSyncState().neq(State.RELATIONSHIP).blockingCount();
    }


    public static List<TrackedEntityInstance> trackedEntityInstanceList() {
        return Sdk.d2().trackedEntityModule().trackedEntityInstances()
                .byAggregatedSyncState().neq(State.RELATIONSHIP)
                .withTrackedEntityAttributeValues()
                .orderByCreated(DESC)
                .orderByLastUpdated(DESC)
                .orderByCreatedAtClient(DESC)
                .blockingGet();
    }

    public static List<ProgramStage> programStages(String programUuid) {
        return Sdk.d2().programModule().programStages()
                .byProgramUid().eq(programUuid)
                .get()
                .blockingGet();
    }

    public static List<ProgramStageSection> programStageSections(String programUuid) {
        return Sdk.d2().programModule()
                .programStageSections()
                .byProgramStageUid().eq(programUuid)
                .withDataElements()
                .blockingGet();
    }

    public static int singleEventCount() {
        return Sdk.d2().eventModule().events()
                .orderByEventDate(DESC)
                .orderByLastUpdated(DESC)
                .byEnrollmentUid().isNull().blockingCount();
    }

    public static TrackedEntityAttribute singleAttribute(String uuid) {
        return Sdk.d2().trackedEntityModule()
                .trackedEntityAttributes()
                .byUid().eq(uuid)
                .one().blockingGet();
    }
    public static List<TrackedEntityAttribute> trackedEntityAttributes() {
        return Sdk.d2().trackedEntityModule()
                .trackedEntityAttributes()
                .get()
                .blockingGet();
    }


    public static List<EventWithOrganization> getAllEventsWithOrganizations() {
        List<Event> events = Sdk.d2().eventModule().events()
                .byEnrollmentUid().isNull()
                .blockingGet();

        List<EventWithOrganization> eventsWithOrganizations = new ArrayList<>();

        for (Event event : events) {
            String enrollmentUid = event.enrollment();
            if (enrollmentUid != null) {
                Enrollment enrollment = Sdk.d2().enrollmentModule().enrollments()
                        .uid(enrollmentUid)
                        .blockingGet();

                String organizationUid = enrollment.organisationUnit();
                // Fetch organization details using the organization UID
                OrganisationUnit organizationUnit = Sdk.d2().organisationUnitModule().organisationUnits()
                        .uid(organizationUid)
                        .blockingGet();

                eventsWithOrganizations.add(new EventWithOrganization(event, organizationUnit));
            }
        }

        return eventsWithOrganizations;
    }

    public List<ProgramStage> getProgramStagesForProgram(String programUid) {
     /*   return Sdk.d2().programModule().programStages()
                .byProgramUid().eq(programUid)
                .get()
                .blockingGet();*/
        try {
            List<ProgramStage> programStages = Sdk.d2().programModule().programStages()
                    .byProgramUid().eq(programUid)
                    .blockingGet();

            // Manually sort by creation time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Collections.sort(programStages, Comparator.comparing(BaseIdentifiableObject::created));
            }

            return programStages;
        } catch (Exception e) {
            // Handle D2Error
            return null; // or throw an exception based on your error handling strategy
        }
    }

    public OrganisationUnit getOrganizationByUuid(String organizationUuid) {
        return Sdk.d2().organisationUnitModule().organisationUnits()
                .uid(organizationUuid)
                .blockingGet();
    }

    public static List<Event> getAllEvents(String uuid) {
        return Sdk.d2().eventModule()
                .events()
                .byEnrollmentUid().eq(uuid)
                .get()
                .blockingGet();
    }

    public static List<Enrollment> getAllEnrolments() {
        return Sdk.d2().enrollmentModule()
                .enrollments()
                .orderByCreated(DESC)
                .orderByLastUpdated(DESC)
                .orderByEnrollmentDate(DESC)
                .get()
                .blockingGet();
    }

    public static Enrollment getSingleEnrollment(String uuid) {
        return Sdk.d2().enrollmentModule()
                .enrollments()
                .byUid().eq(uuid)
                .withNotes()
                .one().blockingGet();
    }

    public static List<Event> getAllEventsWithTrackedEntities() {
        try {
            List<Enrollment> enrollments = Sdk.d2().enrollmentModule().enrollments()
//                    .byEventQuery(EnrollmentQuery.create().events().byEnrollmentUid().isNull())
                    .blockingGet();

            List<Event> eventsWithTrackedEntities = new ArrayList<>();

            for (Enrollment enrollment : enrollments) {
                List<Event> events = Sdk.d2().eventModule().events()
                        .byEnrollmentUid().eq(enrollment.uid())
                        .blockingGet();

                eventsWithTrackedEntities.addAll(events);
            }

            return eventsWithTrackedEntities;
        } catch (Exception d2Error) {
            // Handle D2Error
            return new ArrayList<>(); // or throw an exception based on your error handling strategy
        }
    }

    public static int dataValueCount() {
        return Sdk.d2().dataValueModule().dataValues().blockingCount();
    }


}
