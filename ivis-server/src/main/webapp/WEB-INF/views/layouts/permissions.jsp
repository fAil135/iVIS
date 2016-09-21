<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/resources/js/jquery.tristate.js" var="JQueryTristateUrl"/>
<head>
    <script src="${JQueryTristateUrl}"></script>
</head>

<h1>Permissions of ${specify}</h1>

<form:form modelAttribute="allowedMethods" action="${identifier}?permit" class="${specify}-permissions-form" method="post">

    <div class="field">
        <label>Service method provided by entities:</label>
    </div>
    <c:forEach items="${entityRestProviderInformationList}" var="info" >
        <div class="field">
            <label for="entity${info.id}">
                <span class="arrow" onclick="showOrHideElementByLabel(this);"></span>${info.entityClass}
                <input type="checkbox" class="tristate" onclick="setClicked();" for="entity${info.id}"/>
            </label>
        </div>
        <div id="entity${info.id}" class="non-display indent">
            <div class="field">
                <label>Methods of entity</label>
            </div>
            <c:forEach items="${info.entityProviderMethods}" var="method">
                <div class="field">
                    <label for="method${method.id}">
                        <span class="arrow" onclick="showOrHideElementByLabel(this);"></span>${method.name}
                    </label>
                </div>
                <div id="method${method.id}" class="non-display indent">
                    <div class="field">
                        <label>Url: ${method.url}</label>
                    </div>
                    <div class="field">
                        <label>Request method: ${method.requestMethod.toString()}</label>
                    </div>
                    <div class="field">
                        <label>Out parameter: ${method.outParameter}</label>
                    </div>
                    <div class="field" onclick="showOrHideNextElement(this);">
                        <label for="inparameters${method.id}">
                            <span class="arrow" onclick="showOrHideElementByLabel(this);"></span>In parameters
                        </label>
                    </div>
                    <div id="inparameters${method.id}" class="non-display indent">
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


    <form:checkboxes path="collection" items="${methodRestProviderForEntityList}" itemLabel="name" cssErrorClass="error"
                     cssClass="check-box"/>

    <div class="buttons">
        <button class="positive" type="submit">Save</button>
        <a class="button neutral" onclick="history.back()">Back</a>
    </div>

</form:form>

<script>
    $(document).ready(function () {

        $("input[id^='collection']").each(function (index, element) {

            var $tristate = $('.tristate');
            $tristate.tristate({
                checked:            "Checked all",
                unchecked:          "Unchecked all",
                indeterminate:      "Some checked",

                change:             tristateOnChange,
                init:               tristateOnInit

            });

            $(element).click(function () {
                var entityDivId = this.parentElement.parentElement.parentElement.id;
                var state = calcState(entityDivId);
                setState(entityDivId, state);
            });

            var idOfMethod = "method" + element.getAttribute("value");
            $("label[for='" + idOfMethod + "']").append(element);
            $("label[for='" + element.id + "']").parent().remove();

        });

    });
</script>


