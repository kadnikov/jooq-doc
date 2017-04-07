<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=768">
    <meta name="description" content="">
    <title translate="app.title.label"></title>
    <link rel="stylesheet" href="${contextPath}/frontend/css/app.css">

    <!--[if lte IE 7]>
    <script src="${contextPath}/js/lib/json3.min.js"></script>
    <![endif]-->

    <!--[if lt IE 9]>
    <script src="${contextPath}/js/lib/es5-shim.min.js"></script>
    <![endif]-->

    <script>
        // include angular loader, which allows the files to load in any order
        <%@ include file="/static/lib/angular-loader.js" %>
        <%@ include file="/static/lib/script-loader.js" %>

        // load all of the dependencies asynchronously.
        $script([
            '${contextPath}/frontend/js/vendor.min.js',
            '${contextPath}/frontend/js/app.min.js',
            '${contextPath}/frontend/js/partials.js'
        ], function () {
            // when all is done, execute bootstrap angular application
            angular.bootstrap(document, ['app']);
        });
    </script>
</head>
<body>

<div class="wrapper" ng-cloak class="ng-cloak">

    <nav class="navbar navbar-default" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="#" translate="header.brand.label"></a>
            </div>
            <div class="collapse navbar-collapse">
                <div ng-include="'frontend/partials/search/search-form.html'"></div>
            </div>
        </div>
    </nav>

    <div growl></div>
    <div class="main-content container-fluid" ui-view autoscroll="false"></div>

    <div class="push"></div>
</div>

<footer ng-cloak class="ng-cloak">
    <div class="container-fluid">
        <p class="text-muted text-center" translate="footer.message">
        </p>
    </div>
</footer>

</body>
</html>
