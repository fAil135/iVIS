    function deleteElement(url, id){
    if(confirm("do you want to delete application?")){
       deleteElementRest(url, id,
           function(){
               $("tr[data-object-id='" + id + "']").remove();
       });
    };
};

function deleteElementRest(url, id, callback) {
    $.ajax(
        {
            url: url + "/" + id,
            success: callback(),
            method: "DELETE",
            error: function (result, arg1) {
                alert(resul)
            }
        }
    )
};

function showOrHideElementByLabel(spanArrow) {
    var label = spanArrow.parentElement;
    var $element = $("#" + label.getAttribute("for"));
    if($element.is(":visible")) {
        $element.hide();
        $(label).find(">:first-child").css('transform', 'rotate(45deg)');
    } else {
        $element.show();
        $(label).find(">:first-child").css('transform', 'rotate(135deg)');
    }
};


function calcState(entityDivId) {
    var $checkboxGroup = $("#" + entityDivId + " :input");
    var $checkboxGroupChecked = $("#" + entityDivId + " :input:checked");
    if ($checkboxGroupChecked.length > 0 && $checkboxGroupChecked.length != $checkboxGroup.length) {
        return null;
    } else {
        return $checkboxGroupChecked.length == $checkboxGroup.length;
    }

}

function setState(entityDivId, state) {
    $("input[for='" + entityDivId + "']").tristate("state", state);
}

function tristateOnChange(state, value) {
    if (state != null) {
        var entityDivId = this[0].getAttribute("for");
        $('#' + entityDivId).find("input[type='checkbox']").prop("checked", state);
    }
}

function tristateOnInit(state, value) {
    var entityDivId = this[0].getAttribute("for");
    var state = calcState(entityDivId);
    setState(entityDivId, state);
}