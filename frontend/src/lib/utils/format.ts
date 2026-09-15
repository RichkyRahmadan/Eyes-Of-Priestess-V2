// src/lib/utils/format.ts
import { format, formatRelative, isToday, isYesterday } from 'date-fns';
import { id } from 'date-fns/locale';

export function formatIDR(amount: number): string {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format(amount);
}

export function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  if (isToday(date)) return `Hari ini, ${format(date, 'HH:mm')}`;
  if (isYesterday(date)) return `Kemarin, ${format(date, 'HH:mm')}`;
  return format(date, 'd MMM yyyy, HH:mm', { locale: id });
}

export function formatDateShort(dateStr: string): string {
  const date = new Date(dateStr);
  if (isToday(date)) return format(date, 'HH:mm');
  if (isYesterday(date)) return 'Kemarin';
  return format(date, 'd MMM', { locale: id });
}

export function formatRelativeTime(dateStr: string): string {
  return formatRelative(new Date(dateStr), new Date(), { locale: id });
}

export function truncate(str: string, max = 50): string {
  if (str.length <= max) return str;
  return str.slice(0, max) + '...';
}

export function getInitials(name: string): string {
  return name
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase();
}
