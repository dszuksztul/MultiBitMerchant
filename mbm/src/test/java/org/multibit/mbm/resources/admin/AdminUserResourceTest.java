package org.multibit.mbm.resources.admin;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.multibit.mbm.api.hal.HalMediaType;
import org.multibit.mbm.api.request.admin.AdminCreateUserRequest;
import org.multibit.mbm.db.DatabaseLoader;
import org.multibit.mbm.db.dao.UserDao;
import org.multibit.mbm.db.dto.Role;
import org.multibit.mbm.db.dto.User;
import org.multibit.mbm.test.BaseJerseyResourceTest;
import org.multibit.mbm.test.FixtureAsserts;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdminUserResourceTest extends BaseJerseyResourceTest {

  private final UserDao userDao=mock(UserDao.class);

  private final AdminUserResource testObject=new AdminUserResource();

  @Override
  protected void setUpResources() {

    // Create the supporting Role
    Role adminRole = DatabaseLoader.buildAdminRole();
    Role customerRole = DatabaseLoader.buildCustomerRole();

    // Create the User for authenticated access
    User adminUser = setUpAuthenticator(Lists.newArrayList(adminRole));
    adminUser.setId(1L);

    // Create the customer Users
    User aliceUser = DatabaseLoader.buildAliceCustomer(customerRole);
    aliceUser.setId(1L);
    User bobUser = DatabaseLoader.buildAliceCustomer(customerRole);
    bobUser.setId(2L);

    // Create pages
    List<User> usersPage1 = Lists.newArrayList();
    usersPage1.add(aliceUser);
    List<User> usersPage2 = Lists.newArrayList();
    usersPage2.add(bobUser);

    // Configure the mock DAO
    when(userDao.getAllByPage(1,1)).thenReturn(usersPage1);
    when(userDao.getAllByPage(1,2)).thenReturn(usersPage2);

    testObject.setUserDao(userDao);

    // Configure resources
    addResource(testObject);

  }

  @Test
  public void testGetAllByPage() throws Exception {

    String actualResponse = client()
      .resource("/admin/user")
      .queryParam("pageSize","1")
      .queryParam("pageNumber", "1")
      .accept(HalMediaType.APPLICATION_HAL_JSON)
      .get(String.class);

    FixtureAsserts.assertStringMatchesJsonFixture("User list can be retrieved as JSON", actualResponse, "fixtures/hal/user/expected-users-by-admin-page-1.json");

    actualResponse = client()
      .resource("/admin/user")
      .queryParam("pageSize","1")
      .queryParam("pageNumber", "2")
      .accept(HalMediaType.APPLICATION_HAL_JSON)
      .get(String.class);

    FixtureAsserts.assertStringMatchesJsonFixture("User list can be retrieved as JSON", actualResponse, "fixtures/hal/user/expected-users-by-admin-page-2.json");

  }

  @Test
  public void testCreateUser() throws Exception {

    AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest();
    createUserRequest.setUsername("charlie");
    createUserRequest.setPassword("charlie1");

    String actualResponse = client()
      .resource("/admin/user")
      .accept(HalMediaType.APPLICATION_HAL_JSON)
      .entity(createUserRequest)
      .post(String.class);

    FixtureAsserts.assertStringMatchesJsonFixture("CreateUser by admin response render to JSON",actualResponse,"fixtures/hal/cart/expected-user-new-by-admin.json");

  }


}