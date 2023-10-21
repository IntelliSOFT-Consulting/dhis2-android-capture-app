package com.nacare.capture.data.service;


import static org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope.OrderByDirection.ASC;
import static org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope.OrderByDirection.DESC;

import android.os.Build;

import com.nacare.capture.models.CodeValue;
import com.nacare.capture.models.EventWithOrganization;
import com.nacare.capture.data.Sdk;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.FilterQueryCriteria;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.AttributeValueFilter;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
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

    public static TrackedEntityInstance getTrackedEntity(String uuid) {
        return Sdk.d2().trackedEntityModule().trackedEntityInstances()
                .byAggregatedSyncState().neq(State.RELATIONSHIP)
                .withTrackedEntityAttributeValues()
                .byUid().eq(uuid)
                .orderByCreated(DESC)
                .orderByLastUpdated(DESC)
                .orderByCreatedAtClient(DESC)
                .one()
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

    public static Event getEventsPerEnrollment(String stage, String enrollmentUid) {
        return Sdk.d2().eventModule().events()
                .orderByEventDate(DESC)
                .orderByLastUpdated(DESC)
                .byEnrollmentUid().eq(enrollmentUid)
                .byProgramStageUid().eq(stage)
                .one()
                .blockingGet();
    }

    public static List<TrackedEntityDataValue> getEventAttribute(String eventUid) {
        return Sdk.d2().trackedEntityModule().trackedEntityDataValues()
//                .byEvent().eq(eventUid)
                .get()
                .blockingGet();
    }

    public static List<Event> getEventDataValuesPerEnrollment(String evenUid) {
        return Sdk.d2().eventModule().events()
                .byUid().eq(evenUid)
                .get()
                .blockingGet();
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
                .withLegendSets()
                .orderByCreated(ASC)
                .get()
                .blockingGet();
    }

    public static List<TrackedEntityInstance> searchEntityAttributes(FilterQueryCriteria filters) {
        return Sdk.d2().trackedEntityModule()
                .trackedEntityInstanceQuery().get().blockingGet();

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

    public List<ProgramStageSection> getProgramStageSections(String programStageUid) {
        return Sdk.d2().programModule().programStageSections()
                .byProgramStageUid().eq(programStageUid)
                .withDataElements()
                .get()
                .blockingGet();
    }

    public List<Option> getDataElementOptions(String elementUid) {
        return Sdk.d2().optionModule().options().byOptionSetUid()
                .eq(elementUid)
                .get()
                .blockingGet();
    }

    public List<ProgramStageDataElement> getDataElements(String programStageUid) {
        return Sdk.d2().programModule().programStageDataElements()
                .byProgramStage().eq(programStageUid)
                .get()
                .blockingGet();
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

    public static List<Enrollment> getAllActiveEntityEnrolments(String entity) {
        return Sdk.d2().enrollmentModule()
                .enrollments()
                .orderByCreated(DESC)
                .orderByLastUpdated(DESC)
                .orderByEnrollmentDate(DESC)
                .byTrackedEntityInstance().eq(entity)
                .get()
                .blockingGet();
    }

    public static Enrollment getLatestEntityEnrollment(String entity) {
        return Sdk.d2().enrollmentModule()
                .enrollments()
                .orderByEnrollmentDate(DESC)
                .byTrackedEntityInstance().eq(entity)
                .one()
                .blockingGet();
    }

    public static Enrollment getSingleEnrollment(String uuid) {
        return Sdk.d2().enrollmentModule()
                .enrollments()
                .byUid().eq(uuid)
                .withNotes()
                .one().blockingGet();
    }
    public static List<OrganisationUnit> loadOrganizations() {
        return Sdk.d2().organisationUnitModule()
                .organisationUnits()
                .get().blockingGet();
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


    public static void createEvent() {
      /*  try {
            String eventUid = Sdk.d2().eventModule().events().add(
                    EventCreateProjection.create("enrollment", "program", "programStage", "orgUnit", "attCombo"));

            Sdk.d2().eventModule().events().uid(eventUid).setStatus(EventStatus.ACTIVE);
        } catch (D2Error d2Error) {

        }*/
       /* val eventBuilder = EventCreateProjection.builder()
                .program(data.position)
                .organisationUnit(org)
                .programStage(data.id)
                .enrollment(date)
                .build()
        // Create the empty event
        val eventUid = Sdk.d2().eventModule().events()
                .blockingAdd(eventBuilder)
        Sdk.d2().eventModule().events().uid(eventUid).apply {
            setStatus(EventStatus.ACTIVE)
            setEventDate(FormatterClass().parseEventDate(date))
            L*/

    }
}
