import * as Sentry from "@sentry/react-native";
import _ from "lodash";
import Config from "react-native-config";

const isPresent = (obj) => {
    return !_.isNil(obj);
};
const IGNORED_ERROR_MESSAGES = ["Network error: Network request failed"];

const sentryHttpBreadcrumbCategories = ["xhr", "fetch"];
const excludedUrls = [
  "https://clients3.google.com/generate_204",
  `${Config.MOCKINGJAY_API_ROOT}/graphql`,
  Config.EVENTS_URL,
];

function isExcluded(breadcrumb) {
  // In the future, we could support different combos of excluded categories+URLs. At the moment, we want to check for
  // the same categories in all cases, so we can simplify.
  return (
    isPresent(breadcrumb) &&
    sentryHttpBreadcrumbCategories.includes(breadcrumb?.category) &&
    excludedUrls.includes(breadcrumb?.data?.url)
  );
}

export function beforeBreadcrumb(breadcrumb) {
  if (isExcluded(breadcrumb)) {
    return null;
  }

  Object.entries(breadcrumb?.data ?? []).map(([k, v]) => {
    if (typeof v === "boolean") {
      breadcrumb.data[k] = v.toString();
    }
  });

  return breadcrumb;
}

export function beforeSendEvent(event, hint) {
  const errorMessage = _.get(hint, "originalException.message");
  if (IGNORED_ERROR_MESSAGES.includes(errorMessage)) {
    return null;
  }

  if (event?.tags) {
    Object.entries(event.tags).map(([k, v]) => {
      const stringValueDownCased = v?.toString()?.toLowerCase();
      if (stringValueDownCased === "true" || stringValueDownCased === "false") {
        event.tags[k] = stringValueDownCased;
      }
    });
  }

  return event;
}

export class SentryErrorNotifier {
  static Severity = Sentry.Severity;

  static init() {
    Sentry.init({
      environment: Config.ENVIRONMENT,
      dsn:
        "https://9982d4350fe04dbbb10f25e95357a022@o268421.ingest.sentry.io/1475927",
      normalizeDepth: 4, // For the redux enhancer
      enableAutoSessionTracking: true,
      enableOutOfMemoryTracking: false,
      beforeSend: beforeSendEvent,
      beforeBreadcrumb,
    });
  }

  static captureException(
    exception,
    {
      level = SentryErrorNotifier.Severity.Warning,
      tags = {},
      extras = {},
    } = {}
  ) {
    Sentry.withScope((scope) => {
      scope.setLevel(level);
      scope.setTags(tags);
      scope.setExtras(extras);
      Sentry.captureException(exception);
    });
  }

  static recordMessage(
    message,
    { level = SentryErrorNotifier.Severity.Debug, tags = {}, extras = {} } = {}
  ) {
    Sentry.withScope((scope) => {
      scope.setLevel(level);
      scope.setTags(tags);
      scope.setExtras(extras);
      scope.setFingerprint([message]);
      Sentry.captureMessage(message);
    });
  }

  static breadcrumb({ message, category, level, data }) {
    Sentry.addBreadcrumb({
      message: message,
      category: category,
      level: level,
      data: data,
    });
  }

  static setTag(tag, value) {
    Sentry.setTag(tag, value);
  }

  static setExtra(extra, value) {
    Sentry.setExtra(extra, value);
  }

  static setUser(userData) {
    Sentry.setUser(userData);
  }

  static clearUser() {
    Sentry.setUser({});
  }
}
