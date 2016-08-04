<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/users" var="backUrl"/>

<h1>Permissions of user</h1>

<form:form action="" modelAttribute="user" class="user-permissions-form" method="post">

    <div class="field">
        <label>Service method provided by entities:</label>
    </div>
    <c:forEach items="${entityRestProviderInformationList}" var="info" >
        <div class="field">
            <label for="entity${info.id}">${info.entityClass}</label>
        </div>
        <div id="entity${info.id}">
            <div class="field">
                <label>Methods of entity</label>
            </div>
            <c:forEach items="${info.entityProviderMethods}" var="method">
                <div class="field">
                    <label for="method${method.id}">Name: ${method.name}</label>
                </div>
                <div id="method${method.id}" class="non-display">
                    <div class="field">
                        <label>Url: ${method.url}</label>
                    </div>
                    <div class="field">
                        <label>Request method: ${method.requestMethod.toString()}</label>
                    </div>
                    <div class="field">
                        <label>Out parameter: ${method.outParameter}</label>
                    </div>
                    <div class="field">
                        <label for="inparameters${method.id}">In parameters</label>
                    </div>
                    <div id="inparameters${method.id}" class="non-display">
                        <c:forEach items="${method.inParameters}" var="inParameter">
                            <div class="field">
                                <label>Parameter: ${inParameter.key} Type: ${inParameter.value} </label>
                            </div>
                        </c:forEach>
                    </div>

                </div>
            </c:forEach>
        </div>
    </c:forEach>


    <form:checkboxes path="allowedMethods" items="${methodRestProviderForEntityList}" itemLabel="name" cssErrorClass="error"
                     cssClass="check-box" itemValue="id"/>


</form:form>

<script>
    $(document).ready(function () {

    });
</script>

