package pt.ulisboa.tecnico.rnl.dei.ems.security;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;

// Role-based permission mapping (RBAC)
public final class RolePermissions {

	private static final Map<PersonType, Set<Permission>> ROLE_PERMISSIONS = new EnumMap<>(PersonType.class);

	static {
		ROLE_PERMISSIONS.put(PersonType.ADMINISTRATOR, EnumSet.of(
				Permission.PERSON_READ,
				Permission.PERSON_CREATE,
				Permission.PERSON_UPDATE,
				Permission.PERSON_DELETE,
				Permission.SCHOOL_READ,
				Permission.SCHOOL_CREATE,
				Permission.SCHOOL_UPDATE,
				Permission.SCHOOL_DELETE,
				Permission.DISCIPLINE_READ,
				Permission.DISCIPLINE_CREATE,
				Permission.DISCIPLINE_UPDATE,
				Permission.DISCIPLINE_DELETE,
				Permission.EXAM_DISTRIBUTE,
				Permission.REVIEW_DISTRIBUTE,
				Permission.STATISTICS_READ));

		ROLE_PERMISSIONS.put(PersonType.SCHOOL_STAFF, EnumSet.of(
				Permission.PERSON_READ,
				Permission.SCHOOL_READ,
				Permission.DISCIPLINE_READ,
				Permission.EXAM_READ,
				Permission.EXAM_UPLOAD,
				Permission.EXAM_SEGMENT,
				Permission.EXAM_DELETE,
				Permission.EXAM_RELEASE,
				Permission.GRADES_READ,
				Permission.STATISTICS_READ));

		ROLE_PERMISSIONS.put(PersonType.TEACHER, EnumSet.of(
				Permission.DISCIPLINE_READ,
				Permission.EVALUATION_READ,
				Permission.EVALUATION_SUBMIT,
				Permission.REVIEW_EVALUATE,
				Permission.STATISTICS_READ));

		ROLE_PERMISSIONS.put(PersonType.STUDENT, EnumSet.of(
				Permission.EXAM_READ,
				Permission.REVIEW_REQUEST));
	}

	private RolePermissions() {
	}

	public static Set<Permission> forRole(PersonType type) {
		return Collections.unmodifiableSet(
				ROLE_PERMISSIONS.getOrDefault(type, EnumSet.noneOf(Permission.class)));
	}
}
